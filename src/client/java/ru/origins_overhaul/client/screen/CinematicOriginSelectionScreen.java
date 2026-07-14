package ru.origins_overhaul.client.screen;

import io.github.apace100.origins.networking.ChooseOriginPacket;
import io.github.apace100.origins.networking.ChooseRandomOriginPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import ru.origins_overhaul.client.ClientOriginCatalog;
import ru.origins_overhaul.client.ClientSelectionConfig;
import ru.origins_overhaul.client.OriginSelectionLayout;
import ru.origins_overhaul.client.OriginSelectionSession;
import io.github.apace100.origins.screen.OriginsOverhaulWaitForNextLayerScreen;
import ru.origins_overhaul.model.OriginPresentation;
import ru.origins_overhaul.model.PresentedPower;
import ru.origins_overhaul.profiles.OriginDifficultyColorResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CinematicOriginSelectionScreen extends Screen {
    private final OriginSelectionSession session;
    private final boolean debugPreview;
    private OriginSelectionLayout layout;
    private int advantageScroll;
    private int disadvantageScroll;
    private int neutralScroll;
    private boolean listOverlay;
    private String search = "";
    private List<OriginPresentation> searchResults = List.of();

    public CinematicOriginSelectionScreen(OriginSelectionSession session) {
        this(session, false);
    }

    public CinematicOriginSelectionScreen(OriginSelectionSession session, boolean debugPreview) {
        super(Component.empty());
        this.session = session;
        this.debugPreview = debugPreview;
    }

    @Override
    protected void init() { layout = OriginSelectionLayout.calculate(width, height); }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractTransparentBackground(context);
        int alpha = (int) (ClientSelectionConfig.opacity() * 255.0f) << 24;
        context.fill(0, 0, width, height, alpha);
        int edge = Math.min(80, Math.max(16, width / 12));
        context.fill(0, 0, edge, height, 0x22000000);
        context.fill(width - edge, 0, width, height, 0x22000000);
        context.fill(0, 0, width, Math.min(edge, height), 0x18000000);
        context.fill(0, height - Math.min(edge, height), width, height, 0x25000000);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractBackground(context, mouseX, mouseY, delta);
        if (layout == null) layout = OriginSelectionLayout.calculate(width, height);
        if (!session.hasCurrentLayer() || session.currentOrigins().isEmpty()) {
            context.text(font, Component.translatable("origins_overhaul.selection.no_origins"), width / 2 - 45, height / 2, 0xFFFFFFFF, false);
            return;
        }
        OriginPresentation origin = session.selectedOrigin();
        renderHeader(context, origin);
        renderColumn(context, layout.advantages(), origin.advantages(), "origins_overhaul.selection.advantages", "origins_overhaul.selection.no_advantages", advantageScroll, 0xFFFFFFFF);
        renderColumn(context, layout.disadvantages(), origin.disadvantages(), "origins_overhaul.selection.disadvantages", "origins_overhaul.selection.no_disadvantages", disadvantageScroll, 0xFFFFB0B0);
        if (ClientSelectionConfig.showNeutral() && !origin.neutralFeatures().isEmpty()) {
            renderColumn(context, layout.neutral(), origin.neutralFeatures(), "origins_overhaul.selection.features", "origins_overhaul.selection.no_features", neutralScroll, 0xFFE0E0E0);
        }
        renderNavigation(context, origin);
        if (listOverlay) renderListOverlay(context);
    }

    private void renderHeader(GuiGraphicsExtractor context, OriginPresentation origin) {
        context.fill(layout.header().x(), layout.header().y() + layout.header().height() - 1, layout.header().x() + layout.header().width(), layout.header().y() + layout.header().height(), 0x66777777);
        context.item(origin.icon().copy(), layout.header().x() + 4, layout.header().y() + 8);
        String title = origin.name().getString().toUpperCase(Locale.getDefault());
        context.text(font, title, layout.header().x() + 32, layout.header().y() + 17, OriginDifficultyColorResolver.resolve(origin.impact(), origin.customAccentColor().orElse(0)), false);
        if (session.layers().size() > 1) {
            Component progress = Component.translatable("origins_overhaul.selection.layer_progress", session.currentLayerIndex() + 1, session.layers().size());
            context.text(font, progress, layout.header().x() + 32, layout.header().y() + 34, 0xFFAAAAAA, false);
        }
        if (debugPreview) context.text(font, Component.translatable("origins_overhaul.selection.debug_preview"), layout.header().x() + layout.header().width() - 105, layout.header().y() + 17, 0xFFFFAA55, false);
    }

    private void renderColumn(GuiGraphicsExtractor context, OriginSelectionLayout.Rect rect, List<PresentedPower> powers, String headingKey, String emptyKey, int scroll, int accent) {
        context.text(font, Component.translatable(headingKey), rect.x(), rect.y(), 0xFFFFFFFF, false);
        int y = rect.y() + 18 - scroll;
        if (powers.isEmpty()) {
            context.text(font, Component.translatable(emptyKey), rect.x(), rect.y() + 28, 0xFFAAAAAA, false);
            return;
        }
        for (PresentedPower power : powers) {
            List<net.minecraft.util.FormattedCharSequence> desc = font.split(power.description(), Math.max(40, rect.width() - 4));
            int blockHeight = 18 + desc.size() * 10 + 8;
            if (y + blockHeight >= rect.y() && y <= rect.y() + rect.height()) {
                context.text(font, power.name(), rect.x(), y, accent, false);
                int lineY = y + 12;
                for (net.minecraft.util.FormattedCharSequence line : desc) {
                    if (lineY >= rect.y() && lineY < rect.y() + rect.height()) context.text(font, line, rect.x(), lineY, 0xFFB8B8B8, false);
                    lineY += 10;
                }
            }
            y += blockHeight;
        }
    }

    private void renderNavigation(GuiGraphicsExtractor context, OriginPresentation origin) {
        OriginSelectionLayout.Rect nav = layout.navigation();
        context.text(font, "<", nav.x() + 8, nav.y(), 0xFFFFFFFF, false);
        String name = origin.name().getString();
        int nameWidth = font.width(name);
        context.text(font, name, width / 2 - nameWidth / 2, nav.y(), 0xFFFFFFFF, false);
        context.text(font, ">", nav.x() + nav.width() - 14, nav.y(), 0xFFFFFFFF, false);
        context.fill(layout.confirm().x(), layout.confirm().y() - 2, layout.confirm().x() + layout.confirm().width(), layout.confirm().y() + layout.confirm().height(), session.selectionSubmitted() || debugPreview ? 0x66444444 : 0xAA222222);
        context.text(font, Component.translatable("origins_overhaul.selection.select"), layout.confirm().x() + 28, layout.confirm().y() + 4, session.selectionSubmitted() || debugPreview ? 0xFF888888 : 0xFFFFFFFF, false);
        if (session.currentOrigins().size() > ClientSelectionConfig.threshold()) {
            context.fill(layout.listButton().x(), layout.listButton().y(), layout.listButton().x() + layout.listButton().width(), layout.listButton().y() + layout.listButton().height(), 0x99222222);
            context.text(font, Component.translatable("origins_overhaul.selection.origin_list"), layout.listButton().x() + 7, layout.listButton().y() + 6, 0xFFFFFFFF, false);
        }
        if (session.randomAllowed(Minecraft.getInstance().player)) {
            context.text(font, Component.translatable("origins_overhaul.selection.random"), layout.confirm().x() - 72, layout.confirm().y() + 4, 0xFFCCCCCC, false);
        }
    }

    private void renderListOverlay(GuiGraphicsExtractor context) {
        context.fill(0, 0, width, height, 0xDD080808);
        context.text(font, Component.translatable("origins_overhaul.selection.search"), 30, 24, 0xFFFFFFFF, false);
        context.fill(30, 40, width - 30, 59, 0xAA333333);
        context.text(font, search.isEmpty() ? Component.translatable("origins_overhaul.selection.search_hint") : Component.literal(search), 36, 46, 0xFFFFFFFF, false);
        int y = 72;
        for (OriginPresentation origin : searchResults) {
            if (y > height - 18) break;
            context.item(origin.icon().copy(), 34, y - 5);
            context.text(font, origin.name(), 58, y, OriginDifficultyColorResolver.resolve(origin.impact(), origin.customAccentColor().orElse(0)), false);
            if (ClientSelectionConfig.showNamespace()) context.text(font, origin.originId().toString(), 58, y + 11, 0xFF999999, false);
            y += 24;
        }
    }

    private void rebuildSearchResults() {
        String query = search.toLowerCase(Locale.ROOT);
        searchResults = session.currentOrigins().stream().filter(o -> query.isEmpty() || o.name().getString().toLowerCase(Locale.ROOT).contains(query) || o.originId().toString().toLowerCase(Locale.ROOT).contains(query) || o.originId().getNamespace().toLowerCase(Locale.ROOT).contains(query)).toList();
    }

    private void submit() {
        if (debugPreview || session.selectionSubmitted() || Minecraft.getInstance().player == null) return;
        Identifier layer = session.currentLayerId();
        OriginPresentation selected = session.selectedOrigin();
        if (selected == null || ClientOriginCatalog.getOrigin(layer, selected.originId()).isEmpty()) return;
        session.markSubmitted();
        ClientPlayNetworking.send(new ChooseOriginPacket(selected.originId(), layer));
        Minecraft.getInstance().setScreen(new OriginsOverhaulWaitForNextLayerScreen(session));
    }

    private void selectRandom() {
        if (debugPreview || session.selectionSubmitted() || Minecraft.getInstance().player == null || !session.randomAllowed(Minecraft.getInstance().player)) return;
        session.markSubmitted();
        ClientPlayNetworking.send(new ChooseRandomOriginPacket(session.currentLayerId()));
        Minecraft.getInstance().setScreen(new OriginsOverhaulWaitForNextLayerScreen(session));
    }

    private void move(int direction) {
        session.move(direction);
        advantageScroll = disadvantageScroll = neutralScroll = 0;
    }

    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double x = event.x(), y = event.y();
        if (listOverlay) {
            if (y >= 72) {
                int index = (int) ((y - 72) / 24);
                if (index >= 0 && index < searchResults.size()) {
                    OriginPresentation chosen = searchResults.get(index);
                    int base = session.currentOrigins().indexOf(chosen);
                    if (base >= 0) session.select(base);
                    listOverlay = false;
                }
            }
            return true;
        }
        if (layout.confirm().contains(x, y)) { submit(); return true; }
        if (session.randomAllowed(Minecraft.getInstance().player) && x >= layout.confirm().x() - 78 && x < layout.confirm().x() - 4 && y >= layout.confirm().y() - 2 && y < layout.confirm().y() + layout.confirm().height()) { selectRandom(); return true; }
        if (layout.navigation().contains(x, y)) { move(x < width / 2 ? -1 : 1); return true; }
        if (session.currentOrigins().size() > ClientSelectionConfig.threshold() && layout.listButton().contains(x, y)) { listOverlay = true; search = ""; rebuildSearchResults(); return true; }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int amount = (int) Math.signum(scrollY) * 12;
        if (layout.advantages().contains(x, y)) advantageScroll = Math.max(0, advantageScroll - amount);
        else if (layout.disadvantages().contains(x, y)) disadvantageScroll = Math.max(0, disadvantageScroll - amount);
        else if (layout.neutral().contains(x, y)) neutralScroll = Math.max(0, neutralScroll - amount);
        else move(scrollY > 0 ? -1 : 1);
        return true;
    }

    @Override public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (listOverlay) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { listOverlay = false; return true; }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) { search = search.substring(0, search.length() - 1); rebuildSearchResults(); return true; }
            if (key == GLFW.GLFW_KEY_ENTER && !searchResults.isEmpty()) { session.select(session.currentOrigins().indexOf(searchResults.get(0))); listOverlay = false; return true; }
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) { move(-1); return true; }
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) { move(1); return true; }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) { submit(); return true; }
        if (key == GLFW.GLFW_KEY_TAB && session.currentOrigins().size() > ClientSelectionConfig.threshold()) { listOverlay = true; search = ""; rebuildSearchResults(); return true; }
        return super.keyPressed(event);
    }

    @Override public boolean charTyped(CharacterEvent event) {
        if (listOverlay && event.isAllowedChatCharacter() && search.length() < 80) { search += event.codepointAsString(); rebuildSearchResults(); return true; }
        return super.charTyped(event);
    }

    public OriginSelectionSession session() { return session; }
}
