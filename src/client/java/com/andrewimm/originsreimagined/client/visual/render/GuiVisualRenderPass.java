package com.andrewimm.originsreimagined.client.visual.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;
import com.andrewimm.originsreimagined.client.visual.profile.ResolvedVisualProfile;

public final class GuiVisualRenderPass {
    private GuiVisualRenderPass() {}

    public static void render(PlayerModel model, PoseStack pose, MultiBufferSource.BufferSource buffers, ResolvedVisualProfile profile, int light, boolean slim) {
        for (VisualModifier modifier : profile.modifiers()) {
            String type = modifier.type().getPath();
            if (type.equals("texture_overlay") || type.equals("emissive_overlay")) renderOverlay(model, modifier, pose, buffers, light, type.equals("emissive_overlay"));
            else if (type.equals("eye_overlay") && modifier.texture() != null && VisualRenderBridge.previewAppearance() != null) EyeOverlayRenderer.gui(model, modifier, VisualRenderBridge.previewAppearance(), pose, buffers, light, false);
            else if (type.equals("geometry_attachment")) renderAttachment(model, modifier, pose, buffers, light, slim);
        }
    }

    private static void renderOverlay(PlayerModel model, VisualModifier modifier, PoseStack pose, MultiBufferSource.BufferSource buffers, int light, boolean emissive) {
        if (modifier.texture() == null) return;
        int color = net.minecraft.util.ARGB.multiplyAlpha(modifier.color(), modifier.opacity());
        VertexConsumer consumer = buffers.getBuffer(emissive ? RenderTypes.entityTranslucentEmissive(modifier.texture()) : RenderTypes.entityTranslucent(modifier.texture()));
        for (ModelPart part : VisualPartRenderer.partsForGui(model, modifier.parts())) part.render(pose, consumer, light, OverlayTexture.NO_OVERLAY, color);
    }

    private static void renderAttachment(PlayerModel model, VisualModifier modifier, PoseStack pose, MultiBufferSource.BufferSource buffers, int light, boolean slim) {
        if (modifier.texture() == null) return;
        ModelPart anchor = VisualPartRenderer.anchor(model, modifier.anchor());
        if (anchor == null) return;
        ModelPart attachment = AttachmentModelCache.get(modifier, slim);
        pose.pushPose();
        anchor.translateAndRotate(pose);
        pose.translate(modifier.offset()[0], modifier.offset()[1], modifier.offset()[2]);
        if (modifier.geometryType().equals("segmented_chain")) {
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(modifier.baseRotation()[0]));
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(modifier.baseRotation()[1]));
            pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(modifier.baseRotation()[2]));
            int index = 0;
            for (ModelPart segment : AttachmentModelCache.getChain(modifier, slim)) {
                pose.pushPose();
                if (index > 0) pose.translate(modifier.segmentOffset()[0], modifier.segmentOffset()[1], modifier.segmentOffset()[2]);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(index * modifier.bendYaw()));
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(index * modifier.bendPitch()));
                segment.render(pose, buffers.getBuffer(RenderTypes.entityTranslucent(modifier.texture())), light, OverlayTexture.NO_OVERLAY, modifier.color());
                pose.popPose(); index++;
            }
            pose.popPose(); return;
        }
        pose.scale(modifier.scale()[0], modifier.scale()[1], modifier.scale()[2]);
        attachment.setRotation((float)Math.toRadians(modifier.rotation()[0]), (float)Math.toRadians(modifier.rotation()[1]), (float)Math.toRadians(modifier.rotation()[2]));
        attachment.render(pose, buffers.getBuffer(RenderTypes.entityTranslucent(modifier.texture())), light, OverlayTexture.NO_OVERLAY, modifier.color());
        attachment.resetPose();
        pose.popPose();
    }
}
