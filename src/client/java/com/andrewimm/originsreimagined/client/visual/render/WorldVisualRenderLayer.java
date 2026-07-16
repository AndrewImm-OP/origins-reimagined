package com.andrewimm.originsreimagined.client.visual.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.Player;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;
import com.andrewimm.originsreimagined.client.visual.context.WorldPlayerVisualContext;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;
import com.andrewimm.originsreimagined.client.visual.profile.ResolvedVisualProfile;
import com.andrewimm.originsreimagined.client.visual.profile.VisualProfileResolver;

public final class WorldVisualRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public WorldVisualRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
        Minecraft client = Minecraft.getInstance();
        if (!com.andrewimm.originsreimagined.client.ClientSelectionConfig.visualsEnabled() || client.level == null) return;
        if (!(client.level.getEntity(state.id) instanceof Player player)) return;
        if (!com.andrewimm.originsreimagined.client.ClientSelectionConfig.showOtherVisuals() && player != client.player) return;
        if (!(player instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer)) return;
        PlayerAppearanceSnapshot appearance = PlayerAppearanceSnapshot.from(clientPlayer, client.options);
        WorldPlayerVisualContext context = new WorldPlayerVisualContext(player, appearance, limbDistance);
        ResolvedVisualProfile profile = VisualProfileResolver.resolve(context, true, VisualBackendCapabilities.WORLD);
        for (VisualModifier modifier : profile.modifiers()) {
            String type = modifier.type().getPath();
            if (type.equals("texture_overlay")) VisualPartRenderer.renderOverlay(getParentModel(), modifier, poseStack, collector, light, state, false);
            else if (type.equals("emissive_overlay")) VisualPartRenderer.renderOverlay(getParentModel(), modifier, poseStack, collector, light, state, true);
            else if (type.equals("eye_overlay") && modifier.texture() != null) EyeOverlayRenderer.world(getParentModel(), modifier, appearance, poseStack, collector, light, state, appearance.modelType() == net.minecraft.world.entity.player.PlayerModelType.SLIM);
            else if (type.equals("geometry_attachment")) VisualPartRenderer.renderAttachment(getParentModel(), modifier, poseStack, collector, light, state, appearance.modelType() == net.minecraft.world.entity.player.PlayerModelType.SLIM);
        }
    }
}
