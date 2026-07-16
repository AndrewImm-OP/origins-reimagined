package com.andrewimm.originsreimagined.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.client.ClientSelectionConfig;
import com.andrewimm.originsreimagined.client.preview.PlayerPreviewController;
import com.andrewimm.originsreimagined.client.visual.context.PreviewPlayerVisualContext;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;
import com.andrewimm.originsreimagined.client.visual.profile.PreviewState;
import com.andrewimm.originsreimagined.client.visual.profile.ResolvedVisualProfile;
import com.andrewimm.originsreimagined.client.visual.profile.VisualProfile;
import com.andrewimm.originsreimagined.client.visual.profile.VisualProfileManager;
import com.andrewimm.originsreimagined.client.visual.profile.VisualProfileResolver;
import com.andrewimm.originsreimagined.client.visual.render.PreviewParticleSystem;
import com.andrewimm.originsreimagined.client.visual.render.VisualBackendCapabilities;
import com.andrewimm.originsreimagined.client.visual.render.VisualRenderCapability;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class VisualTestLabScreen extends Screen {
    private final PlayerPreviewController preview = new PlayerPreviewController();
    private final PreviewParticleSystem particles = new PreviewParticleSystem();
    private List<VisualProfile> profiles = List.of();
    private List<VisualProfile> visibleProfiles = List.of();
    private int selected;
    private String search = "";
    private String modelOverride = "AUTO";
    private boolean inWater, swimming, sneaking, fallFlying, onFire;
    private final Set<Identifier> simulatedPowers = new java.util.HashSet<>();
    private final EnumSet<VisualRenderCapability> disabled = EnumSet.noneOf(VisualRenderCapability.class);
    private boolean cleanCapture;
    private float lastDelta;

    public VisualTestLabScreen() { super(Component.literal("Visual Test Lab")); }

    @Override protected void init() {
        reloadProfiles();
        preview.initialize(Minecraft.getInstance());
        select(0);
    }

    private void reloadProfiles() {
        profiles = VisualProfileManager.snapshot().values().stream().sorted(Comparator.comparing(profile -> profile.profileId().toString())).toList();
        filterProfiles();
    }
    private void filterProfiles() {
        String needle = search.toLowerCase(Locale.ROOT);
        visibleProfiles = profiles.stream().filter(profile -> needle.isBlank() || profile.profileId().toString().toLowerCase(Locale.ROOT).contains(needle) || profile.originId().toString().toLowerCase(Locale.ROOT).contains(needle)).toList();
        if (selected >= visibleProfiles.size()) selected = Math.max(0, visibleProfiles.size() - 1);
    }
    private VisualProfile current() { return visibleProfiles.isEmpty() ? null : visibleProfiles.get(Math.floorMod(selected, visibleProfiles.size())); }
    private Set<VisualRenderCapability> capabilities() {
        EnumSet<VisualRenderCapability> result = EnumSet.copyOf(VisualBackendCapabilities.PREVIEW);
        result.removeAll(disabled);
        return result;
    }
    private void select(int index) {
        if (visibleProfiles.isEmpty()) return;
        selected = Math.floorMod(index, visibleProfiles.size());
        VisualProfile profile = current();
        preview.setOriginContext(null, profile.originId(), profile.profileId(), 1.0f);
        applySimulation();
        particles.clear();
    }
    private void applySimulation() {
        preview.configureSimulation(simulatedPowers, inWater, swimming, sneaking, fallFlying, onFire, modelOverride);
    }
    private ResolvedVisualProfile resolved() {
        VisualProfile profile = current();
        if (profile == null || preview.state().appearance() == null) return ResolvedVisualProfile.empty();
        var context = new PreviewPlayerVisualContext(preview.state().appearance().playerId(), profile.originId(), preview.state().appearance(), simulatedPowers, 0, inWater, swimming, sneaking, fallFlying, onFire);
        return VisualProfileResolver.resolve(profile.profileId(), context, true, capabilities());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        lastDelta = Math.min(delta, 0.1f);
        preview.update(lastDelta, Minecraft.getInstance());
        ResolvedVisualProfile profile = resolved();
        particles.update(lastDelta, profile);
        context.fill(0, 0, width, height, 0xFF101218);
        if (cleanCapture) { renderClean(context, profile); return; }
        context.text(font, Component.literal("VISUAL TEST LAB"), 12, 10, 0xFFFFFFFF, false);
        context.text(font, Component.literal("debug mode: " + OriginsReimagined.DEBUG), 12, 23, 0xFF888888, false);
        renderProfileList(context);
        int px = width / 2 - 115, py = 45, pw = Math.min(250, width / 3), ph = Math.min(330, height - 105);
        context.outline(px, py, pw, ph, 0xFF667080);
        preview.render(context, px, py, pw, ph, 1.0f);
        particles.render(context, px, py, pw, ph);
        renderInfo(context, profile, px + pw + 16, 45);
        renderControls(context);
    }
    private void renderClean(GuiGraphicsExtractor context, ResolvedVisualProfile profile) {
        int px = width / 2 - Math.min(180, width / 4), py = 30, size = Math.min(360, Math.min(width - 20, height - 50));
        preview.render(context, px, py, size, size, 1.0f); particles.render(context, px, py, size, size);
    }
    private void renderProfileList(GuiGraphicsExtractor context) {
        context.text(font, Component.literal("PROFILES"), 12, 45, 0xFF9BD8FF, false);
        int y = 60;
        for (int i = 0; i < Math.min(visibleProfiles.size(), Math.max(1, height / 20 - 5)); i++) {
            VisualProfile profile = visibleProfiles.get(i); int color = i == selected ? 0xFF54FF7A : 0xFFD0D0D0;
            context.text(font, Component.literal(profile.profileId().toString()), 12, y + i * 18, color, false);
        }
        context.text(font, Component.literal("search: " + search + "_"), 12, height - 42, 0xFFAAAAAA, false);
    }
    private void renderInfo(GuiGraphicsExtractor context, ResolvedVisualProfile profile, int x, int y) {
        VisualProfile selectedProfile = current();
        context.text(font, Component.literal("origin=" + (selectedProfile == null ? "none" : selectedProfile.originId())), x, y, 0xFFFFFFFF, false);
        context.text(font, Component.literal("model=" + modelOverride), x, y + 16, 0xFFD0D0D0, false);
        context.text(font, Component.literal("state=" + stateName()), x, y + 32, 0xFFD0D0D0, false);
        context.text(font, Component.literal("ACTIVE MODIFIERS"), x, y + 58, 0xFF9BD8FF, false);
        int row = y + 74;
        for (VisualModifier modifier : profile.modifiers()) { context.text(font, Component.literal(modifier.id() + "  " + modifier.type().getPath()), x, row, 0xFFFFFFFF, false); row += 14; }
        context.text(font, Component.literal("UNSUPPORTED: " + profile.unsupportedModifiers().size()), x, row + 4, profile.unsupportedModifiers().isEmpty() ? 0xFF77DD77 : 0xFFFFAA55, false);
        row += 26;
        context.text(font, Component.literal("CAPABILITIES"), x, row, 0xFF9BD8FF, false);
        for (VisualRenderCapability capability : VisualRenderCapability.values()) { row += 14; boolean active = capabilities().contains(capability); context.text(font, Component.literal((active ? "ON  " : "OFF ") + capability), x, row, active ? 0xFF77DD77 : 0xFF888888, false); }
    }
    private String stateName() { if (inWater) return "UNDERWATER"; if (onFire) return "ON_FIRE"; if (swimming) return "SWIMMING"; if (sneaking) return "SNEAKING"; if (fallFlying) return "FALL_FLYING"; return simulatedPowers.isEmpty() ? "NORMAL" : "POWER_ACTIVE"; }
    private void renderControls(GuiGraphicsExtractor context) {
        int y = height - 22; context.text(font, Component.literal("[C] MODEL  [1] POWER  [2] WATER  [3] SWIM  [4] FIRE  [F8] CLEAN  [R] RESET  [F5] REPORT"), 12, y, 0xFFAAAAAA, false);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x(), y = event.y();
        int listLimit = Math.min(visibleProfiles.size(), Math.max(1, height / 20 - 5));
        if (x < 300 && y >= 55 && y < 55 + listLimit * 18) { select((int)((y - 55) / 18)); return true; }
        int px = width / 2 - 115, py = 45, pw = Math.min(250, width / 3), ph = Math.min(330, height - 105);
        if (x >= px && x < px + pw && y >= py && y < py + ph) return preview.input().press(event.button(), doubleClick, x, y, preview.camera());
        return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) { return preview.input().drag(dx, dy, ClientSelectionConfig.previewMouseSensitivity(), preview.camera()); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return preview.input().release(); }
    @Override public boolean mouseScrolled(double x, double y, double sx, double sy) { preview.input().scroll(sy, ClientSelectionConfig.previewZoomSensitivity(), preview.camera()); return true; }
    @Override public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        if (key == GLFW.GLFW_KEY_LEFT) { select(selected - 1); return true; }
        if (key == GLFW.GLFW_KEY_RIGHT) { select(selected + 1); return true; }
        if (key == GLFW.GLFW_KEY_C) { modelOverride = modelOverride.equals("AUTO") ? "CLASSIC" : modelOverride.equals("CLASSIC") ? "SLIM" : "AUTO"; applySimulation(); return true; }
        if (key == GLFW.GLFW_KEY_1) { togglePower("origins:phantomize"); return true; }
        if (key == GLFW.GLFW_KEY_2) { inWater = !inWater; applySimulation(); return true; }
        if (key == GLFW.GLFW_KEY_3) { swimming = !swimming; applySimulation(); return true; }
        if (key == GLFW.GLFW_KEY_4) { onFire = !onFire; applySimulation(); return true; }
        if (key == GLFW.GLFW_KEY_F8) { cleanCapture = !cleanCapture; return true; }
        if (key == GLFW.GLFW_KEY_R) { preview.reset(); simulatedPowers.clear(); inWater = swimming = sneaking = fallFlying = onFire = false; applySimulation(); return true; }
        if (key == GLFW.GLFW_KEY_F5) { exportReport(); return true; }
        if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) { search = search.substring(0, search.length() - 1); filterProfiles(); return true; }
        return true;
    }
    private void togglePower(String id) { Identifier power = Identifier.parse(id); if (!simulatedPowers.add(power)) simulatedPowers.remove(power); applySimulation(); }
    @Override public boolean charTyped(CharacterEvent event) { if (event.isAllowedChatCharacter() && search.length() < 80) { search += event.codepointAsString(); filterProfiles(); return true; } return true; }
    private void exportReport() {
        String report = "Origins: Reimagined Visual Report\nMinecraft=26.1.2\nprofile=" + (current() == null ? "none" : current().profileId()) + "\nmodel=" + modelOverride + "\nstate=" + stateName() + "\nactive=" + resolved().modifiers().stream().map(VisualModifier::id).toList() + "\nunsupported=" + resolved().unsupportedModifiers() + "\nconfig_particles=" + ClientSelectionConfig.visualParticles() + "\n";
        try { Files.writeString(Minecraft.getInstance().gameDirectory.toPath().resolve("logs/origins-reimagined-visual-report.txt"), report); Minecraft.getInstance().keyboardHandler.setClipboard(report); } catch (Exception exception) { OriginsReimagined.LOGGER.warn("Unable to export visual report", exception); }
    }
    @Override public void removed() { preview.clear(); particles.clear(); super.removed(); }
    @Override public boolean shouldCloseOnEsc() { return true; }
}
