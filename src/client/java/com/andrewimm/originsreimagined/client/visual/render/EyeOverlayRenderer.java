package com.andrewimm.originsreimagined.client.visual.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;
import com.andrewimm.originsreimagined.client.visual.anchor.EyeAnchor;
import com.andrewimm.originsreimagined.client.visual.anchor.EyeAnchorProfile;
import com.andrewimm.originsreimagined.client.visual.anchor.SkinAnchorManager;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;

public final class EyeOverlayRenderer {
    private EyeOverlayRenderer() {}

    public static void world(PlayerModel model, VisualModifier modifier, PlayerAppearanceSnapshot appearance, PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, boolean slim) {
        ModelPart head = model.head;
        EyeAnchorProfile anchors = SkinAnchorManager.get(appearance);
        for (EyeAnchor eye : new EyeAnchor[]{anchors.leftEye(), anchors.rightEye()}) {
            if (eye.width() <= 0 || eye.height() <= 0) continue;
            pose.pushPose();
            head.translateAndRotate(pose);
            position(pose, eye);
            ModelPart geometry = AttachmentModelCache.get(modifier, slim);
            collector.submitModelPart(geometry, pose, RenderTypes.entityTranslucentEmissive(modifier.texture()), light, OverlayTexture.NO_OVERLAY, null, false, false);
            pose.popPose();
        }
    }

    public static void gui(PlayerModel model, VisualModifier modifier, PlayerAppearanceSnapshot appearance, PoseStack pose, MultiBufferSource.BufferSource buffers, int light, boolean slim) {
        EyeAnchorProfile anchors = SkinAnchorManager.get(appearance);
        for (EyeAnchor eye : new EyeAnchor[]{anchors.leftEye(), anchors.rightEye()}) {
            if (eye.width() <= 0 || eye.height() <= 0) continue;
            pose.pushPose();
            model.head.translateAndRotate(pose);
            position(pose, eye);
            ModelPart geometry = AttachmentModelCache.get(modifier, slim);
            VertexConsumer consumer = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(modifier.texture()));
            geometry.render(pose, consumer, light, OverlayTexture.NO_OVERLAY, modifier.color());
            geometry.resetPose();
            pose.popPose();
        }
    }

    private static void position(PoseStack pose, EyeAnchor eye) {
        pose.translate(-4.0f + eye.x() + eye.width() / 2.0f, -8.0f + eye.y() + eye.height() / 2.0f, -4.05f);
        pose.scale(Math.max(0.01f, eye.width() / 2.0f), Math.max(0.01f, eye.height() / 2.0f), 0.08f);
    }
}
