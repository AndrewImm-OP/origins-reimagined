package ru.origins_overhaul.client.screen;

import io.github.apace100.origins.networking.ChooseOriginPacket;
import io.github.apace100.origins.networking.ChooseRandomOriginPacket;
import io.github.apace100.origins.screen.OriginsOverhaulWaitForNextLayerScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
import ru.origins_overhaul.OriginsOverhaul;
import ru.origins_overhaul.client.ClientOriginCatalog;
import ru.origins_overhaul.client.ClientSelectionConfig;
import ru.origins_overhaul.client.OriginSelectionLayout;
import ru.origins_overhaul.client.OriginSelectionSession;
import ru.origins_overhaul.client.OriginSelectionTheme;
import ru.origins_overhaul.client.animation.AnimatedOriginContent;
import ru.origins_overhaul.client.animation.AnimatedPowerContent;
import ru.origins_overhaul.client.animation.AnimatedRenderContext;
import ru.origins_overhaul.client.animation.AnimationClock;
import ru.origins_overhaul.client.animation.AnimationState;
import ru.origins_overhaul.client.animation.AnimatedValue;
import ru.origins_overhaul.client.animation.AnimatedTextLine;
import ru.origins_overhaul.client.animation.Easing;
import ru.origins_overhaul.client.animation.OriginTransitionController;
import ru.origins_overhaul.client.animation.TextRevealController;
import ru.origins_overhaul.client.preview.PlayerPreviewController;
import ru.origins_overhaul.client.visual.anchor.EyeAnchor;
import ru.origins_overhaul.client.visual.anchor.EyeAnchorProfile;
import ru.origins_overhaul.client.visual.anchor.EyePreset;
import ru.origins_overhaul.client.visual.anchor.SkinAnchorManager;
import ru.origins_overhaul.model.OriginPresentation;
import ru.origins_overhaul.model.PresentedPower;
import ru.origins_overhaul.profiles.OriginDifficultyColorResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CinematicOriginSelectionScreen extends Screen {
    private final OriginSelectionSession session;
    private final boolean debugPreview;
    private final AnimationClock clock = new AnimationClock();
    private final TextRevealController reveal = new TextRevealController();
    private final AnimatedValue backgroundOpacity = new AnimatedValue(0.0f);
    private final AnimatedValue entrance = new AnimatedValue(0.0f);
    private final AnimatedValue overlayOpacity = new AnimatedValue(0.0f);
    private final Map<CacheKey, AnimatedOriginContent> contentCache = new HashMap<>();
    private PlayerPreviewController preview;
    private OriginSelectionLayout layout;
    private OriginTransitionController transitions;
    private AnimationState phase = AnimationState.ENTERING;
    private float iconRotation;
    private float iconPulse;
    private float lastDelta;
    private int advantageScroll;
    private int disadvantageScroll;
    private int neutralScroll;
    private boolean listOverlay;
    private String search = "";
    private List<OriginPresentation> searchResults = List.of();
    private boolean eyeAnchorEditor;
    private boolean editingLeftEye = true;
    private boolean layoutHasNeutral;

    public CinematicOriginSelectionScreen(OriginSelectionSession session) { this(session, false); }

    public CinematicOriginSelectionScreen(OriginSelectionSession session, boolean debugPreview) {
        super(Component.empty());
        this.session = session;
        this.debugPreview = debugPreview;
    }

    @Override
    protected void init() {
        refreshLayout();
        contentCache.clear();
        transitions = new OriginTransitionController(session.currentOrigins().size(), session.selectedOriginIndex());
        transitions.durations(ClientSelectionConfig.transitionOut(), ClientSelectionConfig.transitionIn());
        float opacity = ClientSelectionConfig.opacity();
        backgroundOpacity.start(0.0f, opacity, ClientSelectionConfig.reduceMotion() ? 0.0f : 0.22f, Easing.EASE_OUT_CUBIC);
        entrance.start(0.0f, 1.0f, ClientSelectionConfig.reduceMotion() ? 0.08f : 0.28f, Easing.EASE_OUT_CUBIC);
        reveal.reset();
        overlayOpacity.start(0.0f, 0.0f, 0.0f, Easing.LINEAR);
        clock.reset();
        if (preview == null) preview = new PlayerPreviewController();
        preview.initialize(Minecraft.getInstance());
        updatePreviewOrigin();
    }

    private void updateAnimations() {
        float delta = clock.deltaSeconds();
        lastDelta = delta;
        backgroundOpacity.update(delta);
        entrance.update(delta);
        reveal.update(delta);
        boolean transitionEnabled = ClientSelectionConfig.transitionAnimation();
        boolean changed = transitions != null && transitions.update(delta, transitionEnabled);
        if (changed) {
            session.select(transitions.displayedIndex());
            resetContentAnimation();
            updatePreviewOrigin();
        }
        if (transitions != null && transitions.state() == AnimationState.SWITCHING_OUT) phase = AnimationState.SWITCHING_OUT;
        else if (transitions != null && transitions.state() == AnimationState.SWITCHING_IN) phase = AnimationState.SWITCHING_IN;
        else if (phase != AnimationState.SUBMITTING && phase != AnimationState.LEAVING) phase = entrance.finished() ? AnimationState.IDLE : AnimationState.ENTERING;
        if (ClientSelectionConfig.iconRotation()) iconRotation = (iconRotation + delta * ClientSelectionConfig.iconRotationSpeed()) % 360.0f;
        if (ClientSelectionConfig.iconBob()) iconPulse += delta;
        if (preview != null) preview.update(delta, Minecraft.getInstance());
        overlayOpacity.update(delta);
        if (debugPreview && OriginsOverhaul.DEBUG) {
            // Deliberately sampled only during rendering; no per-frame logging.
        }
    }

    private void resetContentAnimation() {
        reveal.reset();
        advantageScroll = disadvantageScroll = neutralScroll = 0;
    }

    private void refreshLayout() {
        OriginPresentation origin = displayedOrigin();
        boolean hasNeutral = origin != null && ClientSelectionConfig.showNeutral() && !origin.neutralFeatures().isEmpty();
        if (layout == null || layoutHasNeutral != hasNeutral || layout.preview().width() != OriginSelectionLayout.calculate(width, height, hasNeutral).preview().width()) {
            layout = OriginSelectionLayout.calculate(width, height, hasNeutral);
            layoutHasNeutral = hasNeutral;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractTransparentBackground(context);
        int alpha = Math.round(backgroundOpacity.value() * 255.0f) << 24;
        context.fill(0, 0, width, height, alpha);
        int edge = Math.min(80, Math.max(16, width / 12));
        context.fill(0, 0, edge, height, AnimatedRenderContext.alpha(0x22000000, backgroundOpacity.value() / Math.max(0.01f, ClientSelectionConfig.opacity())));
        context.fill(width - edge, 0, width, height, AnimatedRenderContext.alpha(0x22000000, backgroundOpacity.value() / Math.max(0.01f, ClientSelectionConfig.opacity())));
        context.fill(0, 0, width, Math.min(edge, height), 0x18000000);
        context.fill(0, height - Math.min(edge, height), width, height, 0x25000000);
        OriginPresentation origin = displayedOrigin();
        if (origin != null) {
            int glow = OriginSelectionTheme.forOrigin(origin).previewGlow();
            int glowWidth = Math.max(100, width / 7);
            context.fillGradient(width / 2 - glowWidth, 0, width / 2 + glowWidth, height,
                AnimatedRenderContext.alpha((glow & 0x00FFFFFF) | 0x12000000, backgroundOpacity.value()), 0x00000000);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        updateAnimations();
        extractBackground(context, mouseX, mouseY, delta);
        refreshLayout();
        if (!session.hasCurrentLayer() || session.currentOrigins().isEmpty()) {
            context.text(font, Component.translatable("origins_overhaul.selection.no_origins"), width / 2 - 45, height / 2, 0xFFFFFFFF, false);
            return;
        }
        OriginPresentation origin = displayedOrigin();
        if (origin == null) return;
        OriginSelectionTheme theme = OriginSelectionTheme.forOrigin(origin);
        renderColumnSurface(context, layout.advantages(), theme.advantageAccent(), false);
        renderColumnSurface(context, layout.disadvantages(), theme.disadvantageAccent(), true);
        if (ClientSelectionConfig.showNeutral() && !origin.neutralFeatures().isEmpty()) renderColumnSurface(context, layout.neutral(), theme.accent(), false);
        renderColumn(context, layout.advantages(), origin.advantages(), "origins_overhaul.selection.advantages", "origins_overhaul.selection.no_advantages", advantageScroll, theme.advantageAccent());
        renderColumn(context, layout.disadvantages(), origin.disadvantages(), "origins_overhaul.selection.disadvantages", "origins_overhaul.selection.no_disadvantages", disadvantageScroll, theme.disadvantageAccent());
        if (ClientSelectionConfig.showNeutral() && !origin.neutralFeatures().isEmpty()) renderColumn(context, layout.neutral(), origin.neutralFeatures(), "origins_overhaul.selection.features", "origins_overhaul.selection.no_features", neutralScroll, 0xFFE0E0E0);
        renderPreview(context);
        renderHeader(context, origin);
        renderNavigation(context, origin);
        if (listOverlay) renderListOverlay(context);
        if (eyeAnchorEditor) renderEyeAnchorEditor(context);
        if (OriginsOverhaul.DEBUG) renderDebug(context);
    }

    private OriginPresentation displayedOrigin() {
        List<OriginPresentation> origins = session.currentOrigins();
        if (origins.isEmpty()) return null;
        int index = transitions == null ? session.selectedOriginIndex() : transitions.displayedIndex();
        return origins.get(Math.floorMod(index, origins.size()));
    }

    private void updatePreviewOrigin() {
        if (preview != null && session.hasCurrentLayer() && displayedOrigin() != null) {
            OriginPresentation origin = displayedOrigin();
            preview.setOriginContext(session.currentLayerId(), origin.originId(), origin.visualProfileId().orElse(origin.originId()), transitions == null ? 1.0f : transitions.progress());
        }
    }

    private void renderPreview(GuiGraphicsExtractor context) {
        OriginSelectionLayout.Rect rect = layout.preview();
        OriginSelectionLayout.Rect body = previewBodyRect();
        OriginSelectionTheme theme = OriginSelectionTheme.forOrigin(displayedOrigin());
        context.fillGradient(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), AnimatedRenderContext.alpha(0x22000000 | (theme.previewGlow() & 0x00FFFFFF), entrance.value()), 0x08000000);
        int border = AnimatedRenderContext.alpha(0x22777777, entrance.value());
        int corner = Math.min(18, Math.min(rect.width(), rect.height()) / 10);
        context.horizontalLine(rect.x(), rect.x() + corner, rect.y(), border);
        context.verticalLine(rect.x(), rect.y(), rect.y() + corner, border);
        context.horizontalLine(rect.x() + rect.width() - corner, rect.x() + rect.width(), rect.y(), border);
        context.verticalLine(rect.x() + rect.width(), rect.y(), rect.y() + corner, border);
        context.horizontalLine(rect.x(), rect.x() + corner, rect.y() + rect.height(), border);
        context.verticalLine(rect.x(), rect.y() + rect.height() - corner, rect.y() + rect.height(), border);
        context.horizontalLine(rect.x() + rect.width() - corner, rect.x() + rect.width(), rect.y() + rect.height(), border);
        context.verticalLine(rect.x() + rect.width(), rect.y() + rect.height() - corner, rect.y() + rect.height(), border);
        renderPreviewPlatform(context, body, theme.previewGlow());
        if (!ClientSelectionConfig.previewEnabled()) return;
        float contentOpacity = transitionOpacity() * entrance.value();
        boolean rendered = preview != null && preview.render(context, body.x(), body.y(), body.width(), body.height(), contentOpacity);
        if (!rendered) context.centeredText(font, Component.translatable("origins_overhaul.selection.preview_unavailable"), body.x() + body.width() / 2, body.y() + body.height() / 2, AnimatedRenderContext.alpha(0xFFAAAAAA, contentOpacity));
    }

    private OriginSelectionLayout.Rect previewBodyRect() {
        OriginSelectionLayout.Rect rect = layout.preview();
        int top = rect.y() + layout.header().height() + 10;
        int bottom = layout.confirm().y() - 42;
        return new OriginSelectionLayout.Rect(rect.x() + 2, top, Math.max(20, rect.width() - 4), Math.max(20, bottom - top));
    }

    private void renderPreviewPlatform(GuiGraphicsExtractor context, OriginSelectionLayout.Rect rect, int color) {
        int center = rect.x() + rect.width() / 2;
        int base = rect.y() + rect.height() - Math.max(18, rect.height() / 12);
        int accent = AnimatedRenderContext.alpha((color & 0x00FFFFFF) | 0x22000000, entrance.value());
        for (int row = 0; row < 3; row++) {
            int half = Math.max(18, rect.width() / 10 - row * 10);
            context.horizontalLine(center - half, center + half, base + row * 2, accent);
        }
    }

    private void renderColumnSurface(GuiGraphicsExtractor context, OriginSelectionLayout.Rect rect, int accent, boolean right) {
        context.fill(rect.x() - 8, rect.y() - 8, rect.x() + rect.width() + 8, rect.y() + rect.height() + 6, AnimatedRenderContext.alpha(0x5908090B, entrance.value()));
        context.fill(rect.x() - 8, rect.y() - 8, rect.x() + rect.width() + 8, rect.y() + 30, AnimatedRenderContext.alpha(0x6613161B, entrance.value()));
        int line = AnimatedRenderContext.alpha((accent & 0x00FFFFFF) | 0x3A000000, entrance.value());
        if (right) context.verticalLine(rect.x() + rect.width() + 7, rect.y() - 8, rect.y() + rect.height() + 6, line);
        else context.verticalLine(rect.x() - 8, rect.y() - 8, rect.y() + rect.height() + 6, line);
        context.horizontalLine(rect.x() - 8, rect.x() + rect.width() + 8, rect.y() - 8, AnimatedRenderContext.alpha(0x22777777, entrance.value()));
        context.horizontalLine(rect.x() - 8, rect.x() + rect.width() + 8, rect.y() + 30, AnimatedRenderContext.alpha((accent & 0x00FFFFFF) | 0x30000000, entrance.value()));
    }

    private AnimatedOriginContent content(OriginPresentation origin, int width) {
        CacheKey key = new CacheKey(origin.originId(), width);
        return contentCache.computeIfAbsent(key, ignored -> AnimatedOriginContent.create(font, origin, width));
    }

    private float transitionOpacity() {
        if (transitions == null) return entrance.value();
        return transitions.state() == AnimationState.SWITCHING_OUT ? transitions.progress() : entrance.value();
    }

    private float transitionOffset() {
        if (transitions == null) return 0.0f;
        if (transitions.state() == AnimationState.SWITCHING_OUT) return -transitions.direction() * 12.0f * (1.0f - transitions.progress());
        if (transitions.state() == AnimationState.SWITCHING_IN) return transitions.direction() * 12.0f * (1.0f - transitions.progress());
        return 0.0f;
    }

    private void renderHeader(GuiGraphicsExtractor context, OriginPresentation origin) {
        float opacity = Math.max(0.0f, Math.min(1.0f, transitionOpacity()));
        int offset = Math.round(transitionOffset());
        OriginSelectionTheme theme = OriginSelectionTheme.forOrigin(origin);
        context.fill(layout.header().x(), layout.header().y() + layout.header().height() - 1, layout.header().x() + layout.header().width(), layout.header().y() + layout.header().height(), AnimatedRenderContext.alpha(0x66777777, opacity));
        float bob = ClientSelectionConfig.iconBob() ? (float) Math.sin(iconPulse * 1.7f) * 1.5f : 0.0f;
        float scale = 1.0f + (float) Math.sin(iconPulse * 2.0f) * 0.0125f;
        context.pose().pushMatrix();
        context.pose().translate(layout.header().x() + 20, layout.header().y() + 20 + bob);
        context.pose().rotate((float) Math.toRadians(iconRotation));
        context.pose().scale(scale * 1.25f, scale * 1.25f);
        context.item(origin.icon().copy(), -8, -8);
        context.pose().popMatrix();
        String title = origin.name().getString().toUpperCase(Locale.getDefault());
        int titleColor = AnimatedRenderContext.alpha(OriginDifficultyColorResolver.resolve(origin.impact(), origin.customAccentColor().orElse(0)), opacity);
        context.text(font, title, layout.header().x() + 52 + offset, layout.header().y() + 20, titleColor, false);
        context.fill(layout.header().x() + 52, layout.header().y() + 42, layout.header().x() + 52 + Math.min(160, font.width(title)), layout.header().y() + 44, AnimatedRenderContext.alpha((theme.accent() & 0x00FFFFFF) | 0x66000000, opacity));
        if (session.layers().size() > 1) context.text(font, Component.translatable("origins_overhaul.selection.layer_progress", session.currentLayerIndex() + 1, session.layers().size()), layout.header().x() + 52 + offset, layout.header().y() + 47, AnimatedRenderContext.alpha(0xFFAAAAAA, opacity), false);
        if (debugPreview) context.text(font, Component.translatable("origins_overhaul.selection.debug_preview"), layout.header().x() + layout.header().width() - 105, layout.header().y() + 17, 0xFFFFAA55, false);
    }

    private void renderColumn(GuiGraphicsExtractor context, OriginSelectionLayout.Rect rect, List<PresentedPower> powers, String headingKey, String emptyKey, int scroll, int accent) {
        float opacity = Math.max(0.0f, Math.min(1.0f, entrance.value() * transitionOpacity()));
        int headingWidth = font.width(Component.translatable(headingKey));
        int headingColor = accent == 0xFFFFFFFF ? 0xFFB9D8C0 : accent;
        context.text(font, Component.translatable(headingKey), rect.x() + Math.max(0, (rect.width() - headingWidth) / 2), rect.y(), AnimatedRenderContext.alpha(headingColor, opacity), false);
        if (powers.isEmpty()) {
            context.text(font, Component.translatable(emptyKey), rect.x(), rect.y() + 50, AnimatedRenderContext.alpha(0xFFAAAAAA, opacity), false);
            return;
        }
        int textWidth = Math.max(40, rect.width() - 28);
        AnimatedOriginContent cached = content(displayedOrigin(), textWidth);
        int effectiveScroll = Math.min(Math.max(0, scroll), maxScroll(rect, powers, textWidth, cached));
        int y = rect.y() + 42 - effectiveScroll;
        int textX = rect.x() + 24;
        context.enableScissor(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height());
        try {
            for (int index = 0; index < powers.size(); index++) {
                PresentedPower power = powers.get(index);
                AnimatedPowerContent animated = cached.power(power.powerId());
                List<FormattedCharSequence> nameLines = font.split(power.name(), textWidth);
                int nameLineCount = Math.max(1, nameLines.size());
                float abilityElapsed = reveal.abilityElapsed(index, ClientSelectionConfig.abilityStagger());
                int visible = animated.visibleCharacters(abilityElapsed, ClientSelectionConfig.textSpeed(), ClientSelectionConfig.textAnimation());
                float abilityOpacity = ClientSelectionConfig.textAnimation() ? Math.min(1.0f, Math.max(0.0f, abilityElapsed / 0.14f)) : 1.0f;
                int blockHeight = nameLineCount * 10 + 2 + animated.lines().size() * 10 + 8;
                if (y + blockHeight >= rect.y() && y <= rect.y() + rect.height()) {
                    int nameColor = AnimatedRenderContext.alpha(accent, abilityOpacity * opacity);
                    context.fill(rect.x() + 5, y + 3, rect.x() + 13, y + 11, nameColor);
                    for (int nameLine = 0; nameLine < nameLines.size(); nameLine++) {
                        context.text(font, nameLines.get(nameLine), textX, y + nameLine * 10, nameColor, false);
                    }
                    int lineY = y + nameLineCount * 10 + 2;
                    for (AnimatedTextLine line : animated.lines()) {
                        if (lineY >= rect.y() && lineY < rect.y() + rect.height()) context.text(font, line.visible(visible), textX, lineY, AnimatedRenderContext.alpha(0xFFB8B8B8, abilityOpacity * opacity), false);
                        lineY += 10;
                    }
                }
                y += blockHeight;
            }
        } finally {
            context.disableScissor();
        }
    }

    private int maxScroll(OriginSelectionLayout.Rect rect, List<PresentedPower> powers, int textWidth, AnimatedOriginContent cached) {
        int total = 0;
        for (PresentedPower power : powers) {
            int nameLines = Math.max(1, font.split(power.name(), textWidth).size());
            total += nameLines * 10 + 2 + cached.power(power.powerId()).lines().size() * 10 + 8;
        }
        return Math.max(0, total - Math.max(0, rect.height() - 42));
    }

    private int scrollBy(int current, int wheelAmount, OriginSelectionLayout.Rect rect, List<PresentedPower> powers) {
        int textWidth = Math.max(40, rect.width() - 28);
        AnimatedOriginContent cached = content(displayedOrigin(), textWidth);
        int max = maxScroll(rect, powers, textWidth, cached);
        if (max == 0) return 0;
        return Math.max(0, Math.min(max, current - wheelAmount));
    }

    private void renderNavigation(GuiGraphicsExtractor context, OriginPresentation origin) {
        float opacity = Math.max(0.0f, Math.min(1.0f, entrance.value() * transitionOpacity()));
        OriginSelectionLayout.Rect nav = layout.navigation();
        OriginSelectionTheme theme = OriginSelectionTheme.forOrigin(origin);
        // The origin name already lives in the header. Keep only unobtrusive navigation glyphs.
        context.text(font, "‹", nav.x() + 8, nav.y() + 3, AnimatedRenderContext.alpha((theme.accent() & 0x00FFFFFF) | 0xFF000000, opacity), false);
        context.text(font, "›", nav.x() + nav.width() - 16, nav.y() + 3, AnimatedRenderContext.alpha((theme.accent() & 0x00FFFFFF) | 0xFF000000, opacity), false);
        int buttonColor = session.selectionSubmitted() || debugPreview ? 0x66444444 : AnimatedRenderContext.alpha(0xAA222222, opacity);
        if (Minecraft.getInstance().player != null && session.randomAllowed(Minecraft.getInstance().player)) {
            context.outline(layout.random().x(), layout.random().y() - 2, layout.random().width(), layout.random().height() + 2, AnimatedRenderContext.alpha(0x33555555, opacity));
            context.text(font, Component.translatable("origins_overhaul.selection.random"), layout.random().x() + 10, layout.random().y() + 4, AnimatedRenderContext.alpha(0xFFCCCCCC, opacity), false);
        }
        context.outline(layout.confirm().x(), layout.confirm().y() - 2, layout.confirm().width(), layout.confirm().height() + 2, AnimatedRenderContext.alpha((theme.accent() & 0x00FFFFFF) | 0x66000000, opacity));
        context.fill(layout.confirm().x(), layout.confirm().y() - 2, layout.confirm().x() + layout.confirm().width(), layout.confirm().y() + layout.confirm().height(), buttonColor);
        context.text(font, Component.translatable("origins_overhaul.selection.select"), layout.confirm().x() + 28, layout.confirm().y() + 4, AnimatedRenderContext.alpha(session.selectionSubmitted() || debugPreview ? 0xFF888888 : 0xFFFFFFFF, opacity), false);
        if (session.currentOrigins().size() > ClientSelectionConfig.threshold()) {
            context.fill(layout.listButton().x(), layout.listButton().y(), layout.listButton().x() + layout.listButton().width(), layout.listButton().y() + layout.listButton().height(), AnimatedRenderContext.alpha(0x99222222, opacity));
            context.text(font, Component.translatable("origins_overhaul.selection.origin_list"), layout.listButton().x() + 7, layout.listButton().y() + 6, AnimatedRenderContext.alpha(0xFFFFFFFF, opacity), false);
        }
    }

    private void renderListOverlay(GuiGraphicsExtractor context) {
        float opacity = overlayOpacity.value();
        context.fill(0, 0, width, height, AnimatedRenderContext.alpha(0xDD080808, opacity));
        int yOffset = Math.round((1.0f - opacity) * 8.0f);
        context.text(font, Component.translatable("origins_overhaul.selection.search"), 30, 24 + yOffset, AnimatedRenderContext.alpha(0xFFFFFFFF, opacity), false);
        context.fill(30, 40 + yOffset, width - 30, 59 + yOffset, AnimatedRenderContext.alpha(0xAA333333, opacity));
        context.text(font, search.isEmpty() ? Component.translatable("origins_overhaul.selection.search_hint") : Component.literal(search), 36, 46 + yOffset, AnimatedRenderContext.alpha(0xFFFFFFFF, opacity), false);
        int y = 72 + yOffset;
        for (OriginPresentation origin : searchResults) {
            if (y > height - 18) break;
            context.item(origin.icon().copy(), 34, y - 5);
            context.text(font, origin.name(), 58, y, AnimatedRenderContext.alpha(OriginDifficultyColorResolver.resolve(origin.impact(), origin.customAccentColor().orElse(0)), opacity), false);
            if (ClientSelectionConfig.showNamespace()) context.text(font, origin.originId().toString(), 58, y + 11, AnimatedRenderContext.alpha(0xFF999999, opacity), false);
            y += 24;
        }
    }

    private void renderEyeAnchorEditor(GuiGraphicsExtractor context) {
        context.fill(0, 0, width, height, 0xDD080808);
        context.text(font, Component.translatable("origins_overhaul.selection.preview_editor"), 24, 22, 0xFFFFFFFF, false);
        context.text(font, Component.translatable("origins_overhaul.selection.preview_editor_hint"), 24, 36, 0xFFAAAAAA, false);
        int size = Math.min(224, Math.max(128, Math.min(width, height) / 2));
        int gx = width / 2 - size / 2;
        int gy = height / 2 - size / 2;
        EyeAnchorProfile profile = preview == null || preview.state().appearance() == null ? EyeAnchorProfile.preset(EyePreset.STANDARD) : SkinAnchorManager.get(preview.state().appearance());
        context.fill(gx - 2, gy - 2, gx + size + 2, gy + size + 2, 0xFF222222);
        for (int cellY = 0; cellY < 8; cellY++) for (int cellX = 0; cellX < 8; cellX++) {
            int color = ((cellX + cellY) & 1) == 0 ? 0xFF454545 : 0xFF353535;
            context.fill(gx + cellX * size / 8, gy + cellY * size / 8, gx + (cellX + 1) * size / 8 - 1, gy + (cellY + 1) * size / 8 - 1, color);
        }
        EyeAnchor selected = editingLeftEye ? profile.leftEye() : profile.rightEye();
        context.fill(gx + selected.x() * size / 8, gy + selected.y() * size / 8, gx + (selected.x() + selected.width()) * size / 8, gy + (selected.y() + selected.height()) * size / 8, 0x8844FF88);
        context.text(font, Component.translatable(editingLeftEye ? "origins_overhaul.selection.preview_left_eye" : "origins_overhaul.selection.preview_right_eye"), gx, gy + size + 16, 0xFFFFFFFF, false);
        context.text(font, Component.translatable("origins_overhaul.selection.preview_editor_close"), gx, gy + size + 32, 0xFFCCCCCC, false);
    }

    private void renderDebug(GuiGraphicsExtractor context) {
        String debug = "phase=" + phase + " progress=" + String.format(Locale.ROOT, "%.2f", transitions == null ? 0.0f : transitions.progress()) + " target=" + (displayedOrigin() == null ? "none" : displayedOrigin().originId()) + " dt=" + String.format(Locale.ROOT, "%.3f", lastDelta);
        context.text(font, debug, 6, height - 10, 0xFFAAAAAA, false);
        if (preview != null && preview.state().appearance() != null) {
            var camera = preview.camera();
            String previewDebug = "preview model=" + preview.state().appearance().modelType() + " yaw=" + String.format(Locale.ROOT, "%.1f", camera.yaw()) + " pitch=" + String.format(Locale.ROOT, "%.1f", camera.pitch()) + " zoom=" + String.format(Locale.ROOT, "%.2f", camera.zoom()) + " offset=(" + String.format(Locale.ROOT, "%.2f", camera.offsetX()) + "," + String.format(Locale.ROOT, "%.2f", camera.offsetY()) + ") skin=" + preview.state().appearance().skinTexture();
            context.text(font, previewDebug, 6, height - 22, 0xFFAAAAAA, false);
        }
    }

    private void rebuildSearchResults() {
        String query = search.toLowerCase(Locale.ROOT);
        searchResults = session.currentOrigins().stream().filter(o -> query.isEmpty() || o.name().getString().toLowerCase(Locale.ROOT).contains(query) || o.originId().toString().toLowerCase(Locale.ROOT).contains(query) || o.originId().getNamespace().toLowerCase(Locale.ROOT).contains(query)).toList();
    }

    private void openList() {
        listOverlay = true;
        search = "";
        rebuildSearchResults();
        overlayOpacity.start(0.0f, 1.0f, ClientSelectionConfig.reduceMotion() ? 0.0f : 0.16f, Easing.EASE_OUT_CUBIC);
    }

    private void closeList() {
        listOverlay = false;
        overlayOpacity.start(overlayOpacity.value(), 0.0f, ClientSelectionConfig.reduceMotion() ? 0.0f : 0.12f, Easing.EASE_OUT_CUBIC);
    }

    private void submit() {
        if (debugPreview || session.selectionSubmitted() || Minecraft.getInstance().player == null) return;
        if (skipCurrentAnimation()) return;
        Identifier layer = session.currentLayerId();
        OriginPresentation selected = displayedOrigin();
        if (selected == null || ClientOriginCatalog.getOrigin(layer, selected.originId()).isEmpty()) return;
        phase = AnimationState.SUBMITTING;
        session.select(transitions.displayedIndex());
        session.markSubmitted();
        ClientPlayNetworking.send(new ChooseOriginPacket(selected.originId(), layer));
        Minecraft.getInstance().setScreen(new OriginsOverhaulWaitForNextLayerScreen(session));
    }

    private void selectRandom() {
        if (debugPreview || session.selectionSubmitted() || Minecraft.getInstance().player == null || !session.randomAllowed(Minecraft.getInstance().player)) return;
        if (skipCurrentAnimation()) return;
        phase = AnimationState.SUBMITTING;
        session.markSubmitted();
        ClientPlayNetworking.send(new ChooseRandomOriginPacket(session.currentLayerId()));
        Minecraft.getInstance().setScreen(new OriginsOverhaulWaitForNextLayerScreen(session));
    }

    private boolean skipCurrentAnimation() {
        boolean active = !entrance.finished() || (!reveal.skipped() && reveal.elapsedSeconds() < 0.9f);
        if (!active) return false;
        entrance.finish();
        backgroundOpacity.finish();
        reveal.skip();
        return true;
    }

    private void move(int direction) {
        if (transitions == null || session.currentOrigins().size() <= 1) return;
        int base = transitions.targetIndex();
        int target = Math.floorMod(base + Integer.signum(direction), session.currentOrigins().size());
        if (transitions.request(target, ClientSelectionConfig.transitionAnimation())) {
            if (!ClientSelectionConfig.transitionAnimation()) session.select(target);
            else { phase = AnimationState.SWITCHING_OUT; iconRotation += direction * 45.0f; }
        }
    }

    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double x = event.x(), y = event.y();
        if (listOverlay) {
            if (y >= 72) {
                int index = (int) ((y - 72) / 24);
                if (index >= 0 && index < searchResults.size()) {
                    int base = session.currentOrigins().indexOf(searchResults.get(index));
                    closeList();
                    if (base >= 0) requestIndex(base);
                }
            }
            return true;
        }
        if (eyeAnchorEditor) {
            if (keyEditorClick(x, y)) return true;
            return true;
        }
        boolean control = layout.confirm().contains(x, y) || layout.random().contains(x, y) || layout.navigation().contains(x, y) || layout.listButton().contains(x, y);
        if (skipCurrentAnimation() && !control) return true;
        if (layout.confirm().contains(x, y)) { submit(); return true; }
        if (Minecraft.getInstance().player != null && session.randomAllowed(Minecraft.getInstance().player) && layout.random().contains(x, y)) { selectRandom(); return true; }
        if (layout.navigation().contains(x, y)) { move(x < width / 2 ? -1 : 1); return true; }
        if (session.currentOrigins().size() > ClientSelectionConfig.threshold() && layout.listButton().contains(x, y)) { openList(); return true; }
        if (previewBodyRect().contains(x, y) && preview != null) {
            if (preview.input().press(event.button(), isDoubleClick, x, y, preview.camera())) return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    private void requestIndex(int index) {
        if (transitions == null) return;
        if (transitions.request(index, ClientSelectionConfig.transitionAnimation())) {
            if (!ClientSelectionConfig.transitionAnimation()) session.select(index);
            else phase = AnimationState.SWITCHING_OUT;
            if (!ClientSelectionConfig.transitionAnimation()) updatePreviewOrigin();
        }
    }

    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (preview != null && preview.input().release()) return true;
        return super.mouseReleased(event);
    }

    @Override public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (preview != null && preview.input().drag(deltaX, deltaY, ClientSelectionConfig.previewMouseSensitivity(), preview.camera())) return true;
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (previewBodyRect().contains(x, y) && preview != null) {
            preview.input().scroll(scrollY, ClientSelectionConfig.previewZoomSensitivity(), preview.camera());
            return true;
        }
        int amount = (int) Math.signum(scrollY) * 12;
        OriginPresentation origin = displayedOrigin();
        if (origin != null && layout.advantages().contains(x, y)) advantageScroll = scrollBy(advantageScroll, amount, layout.advantages(), origin.advantages());
        else if (origin != null && layout.disadvantages().contains(x, y)) disadvantageScroll = scrollBy(disadvantageScroll, amount, layout.disadvantages(), origin.disadvantages());
        else if (origin != null && layout.neutral().contains(x, y)) neutralScroll = scrollBy(neutralScroll, amount, layout.neutral(), origin.neutralFeatures());
        else move(scrollY > 0 ? -1 : 1);
        return true;
    }

    @Override public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (listOverlay) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { closeList(); return true; }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) { search = search.substring(0, search.length() - 1); rebuildSearchResults(); return true; }
            if (key == GLFW.GLFW_KEY_ENTER && !searchResults.isEmpty()) { int index = session.currentOrigins().indexOf(searchResults.get(0)); closeList(); requestIndex(index); return true; }
            return true;
        }
        if (eyeAnchorEditor) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { eyeAnchorEditor = false; return true; }
            if (key == GLFW.GLFW_KEY_TAB) { editingLeftEye = !editingLeftEye; return true; }
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) { move(-1); return true; }
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) { move(1); return true; }
        if (key == GLFW.GLFW_KEY_R && preview != null) { preview.reset(); return true; }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) { submit(); return true; }
        if (key == GLFW.GLFW_KEY_TAB && session.currentOrigins().size() > ClientSelectionConfig.threshold()) { openList(); return true; }
        return super.keyPressed(event);
    }

    @Override public boolean charTyped(CharacterEvent event) {
        if (listOverlay && event.isAllowedChatCharacter() && search.length() < 80) { search += event.codepointAsString(); rebuildSearchResults(); return true; }
        return super.charTyped(event);
    }

    public OriginSelectionSession session() { return session; }
    @Override public void removed() {
        if (preview != null) preview.clear();
        super.removed();
    }
    private record CacheKey(Identifier originId, int width) { }

    private boolean keyEditorClick(double x, double y) {
        if (preview == null || preview.state().appearance() == null) return true;
        int size = Math.min(224, Math.max(128, Math.min(width, height) / 2));
        int gx = width / 2 - size / 2;
        int gy = height / 2 - size / 2;
        if (x < gx || x >= gx + size || y < gy || y >= gy + size) return false;
        int cellX = Math.max(0, Math.min(7, (int) ((x - gx) * 8 / size)));
        int cellY = Math.max(0, Math.min(7, (int) ((y - gy) * 8 / size)));
        EyeAnchorProfile old = SkinAnchorManager.get(preview.state().appearance());
        EyeAnchor selected = editingLeftEye ? old.leftEye() : old.rightEye();
        EyeAnchor replacement = new EyeAnchor(cellX, cellY, selected.width(), selected.height(), selected.layer());
        SkinAnchorManager.put(preview.state().appearance(), editingLeftEye ? new EyeAnchorProfile(EyePreset.CUSTOM, replacement, old.rightEye()) : new EyeAnchorProfile(EyePreset.CUSTOM, old.leftEye(), replacement));
        return true;
    }
}
