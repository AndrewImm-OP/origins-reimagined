package ru.origins_overhaul.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.origins_overhaul.client.ClientSelectionConfig;
import ru.origins_overhaul.client.preview.PlayerAppearanceSnapshot;
import ru.origins_overhaul.client.visual.context.WorldPlayerVisualContext;
import ru.origins_overhaul.client.visual.profile.ResolvedVisualProfile;
import ru.origins_overhaul.client.visual.profile.VisualProfileResolver;
import ru.origins_overhaul.client.visual.render.VisualBackendCapabilities;

public final class VisualWorldDebugScreen extends Screen {
    public VisualWorldDebugScreen() { super(Component.literal("Visual World Debug")); }
    @Override public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(8, 8, 360, 120, 0xCC101218);
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) { context.text(font, Component.literal("player unavailable"), 18, 20, 0xFFFFAA55, false); return; }
        PlayerAppearanceSnapshot appearance = PlayerAppearanceSnapshot.from(client.player, client.options);
        ResolvedVisualProfile profile = VisualProfileResolver.resolve(new WorldPlayerVisualContext(client.player, appearance, delta), true, VisualBackendCapabilities.WORLD);
        context.text(font, Component.literal("VISUAL WORLD DEBUG"), 18, 18, 0xFFFFFFFF, false);
        context.text(font, Component.literal("player=" + client.player.getUUID()), 18, 34, 0xFFD0D0D0, false);
        context.text(font, Component.literal("origin=" + (profile.profile() == null ? "none" : profile.profile().originId())), 18, 50, 0xFFD0D0D0, false);
        context.text(font, Component.literal("profile=" + (profile.profile() == null ? "none" : profile.profile().profileId())), 18, 66, 0xFFD0D0D0, false);
        context.text(font, Component.literal("active modifiers=" + profile.modifiers().size() + " unsupported=" + profile.unsupportedModifiers().size()), 18, 82, 0xFF9BD8FF, false);
        context.text(font, Component.literal("particles=" + ClientSelectionConfig.visualParticles()), 18, 98, 0xFFD0D0D0, false);
    }
    @Override public boolean shouldCloseOnEsc() { return true; }
}
