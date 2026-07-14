package ru.origins_overhaul.client.visual.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import ru.origins_overhaul.client.visual.modifier.VisualModifier;

import java.util.ArrayList;
import java.util.List;

public final class VisualPartRenderer {
    private VisualPartRenderer() {}

    public static void renderOverlay(PlayerModel model, VisualModifier modifier, PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, boolean emissive) {
        if (modifier.texture() == null) return;
        for (ModelPart part : parts(model, modifier.parts())) {
            pose.pushPose();
            part.translateAndRotate(pose);
            int color = modifier.color();
            var renderType = emissive ? RenderTypes.entityTranslucentEmissive(modifier.texture()) : RenderTypes.entityTranslucent(modifier.texture());
            collector.submitModelPart(part, pose, renderType, light, OverlayTexture.NO_OVERLAY, null, false, false);
            pose.popPose();
        }
    }

    public static void renderAttachment(PlayerModel model, VisualModifier modifier, PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, boolean slim) {
        if (modifier.texture() == null) return;
        ModelPart anchor = anchor(model, modifier.anchor());
        if (anchor == null) return;
        ModelPart attachment = AttachmentModelCache.get(modifier, slim);
        pose.pushPose();
        anchor.translateAndRotate(pose);
        pose.translate(modifier.offset()[0], modifier.offset()[1], modifier.offset()[2]);
        pose.scale(modifier.scale()[0], modifier.scale()[1], modifier.scale()[2]);
        attachment.setRotation((float) Math.toRadians(modifier.rotation()[0]), (float) Math.toRadians(modifier.rotation()[1]), (float) Math.toRadians(modifier.rotation()[2]));
        collector.submitModelPart(attachment, pose, RenderTypes.entityTranslucent(modifier.texture()), light, OverlayTexture.NO_OVERLAY, null, false, false);
        attachment.resetPose();
        pose.popPose();
    }

    public static List<ModelPart> partsForGui(PlayerModel model, List<String> names) { return parts(model, names); }
    private static List<ModelPart> parts(PlayerModel model, List<String> names) {
        if (names.isEmpty() || names.stream().anyMatch(name -> name.equalsIgnoreCase("full_model"))) return List.of(model.root());
        List<ModelPart> result = new ArrayList<>();
        for (String name : names) {
            ModelPart part = anchor(model, name);
            if (part != null) result.add(part);
        }
        return result;
    }

    public static ModelPart anchor(PlayerModel model, String name) {
        if (name == null) return model.root();
        return switch (name.toLowerCase(java.util.Locale.ROOT)) {
            case "head", "face", "eyes", "back_of_head" -> model.head;
            case "hat" -> model.hat;
            case "body", "chest", "back", "waist" -> model.body;
            case "left_arm", "left_shoulder", "left_hand" -> model.leftArm;
            case "right_arm", "right_shoulder", "right_hand" -> model.rightArm;
            case "left_sleeve" -> model.leftSleeve;
            case "right_sleeve" -> model.rightSleeve;
            case "left_leg", "left_foot" -> model.leftLeg;
            case "right_leg", "right_foot" -> model.rightLeg;
            case "left_pants" -> model.leftPants;
            case "right_pants" -> model.rightPants;
            default -> null;
        };
    }
}
