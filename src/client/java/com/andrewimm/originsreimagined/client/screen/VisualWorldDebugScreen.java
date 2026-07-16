package com.andrewimm.originsreimagined.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.andrewimm.originsreimagined.client.ClientSelectionConfig;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;
import com.andrewimm.originsreimagined.client.visual.context.WorldPlayerVisualContext;
import com.andrewimm.originsreimagined.client.visual.profile.ResolvedVisualProfile;
import com.andrewimm.originsreimagined.client.visual.profile.VisualProfileResolver;
import com.andrewimm.originsreimagined.client.visual.render.VisualBackendCapabilities;

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
