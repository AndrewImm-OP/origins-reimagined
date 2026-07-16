package com.andrewimm.originsreimagined.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.render.pip.GuiSkinRenderer;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.state.gui.pip.GuiSkinRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.andrewimm.originsreimagined.client.visual.render.VisualRenderBridge;
import com.andrewimm.originsreimagined.client.visual.render.GuiVisualRenderPass;

@Mixin(GuiSkinRenderer.class)
public abstract class GuiSkinRendererVisualMixin {
    @WrapOperation(method = "renderToTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/player/PlayerModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void originsReimagined$applyPreviewTint(PlayerModel model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, Operation<Void> original) {
        if (!VisualRenderBridge.isPreviewModel(model)) { original.call(model, poseStack, consumer, light, overlay); return; }
        int tint = VisualRenderBridge.tint();
        if (tint == 0xFFFFFFFF) original.call(model, poseStack, consumer, light, overlay);
        else model.renderToBuffer(poseStack, consumer, light, overlay, tint);
    }

    @Inject(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiSkinRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private void originsReimagined$renderVisualPass(GuiSkinRenderState state, PoseStack poseStack, CallbackInfo info) {
        if (!VisualRenderBridge.isPreviewModel(state.playerModel())) return;
        var buffers = ((PictureInPictureRendererAccessor) (Object) this).originsReimagined$getBufferSource();
        GuiVisualRenderPass.render(state.playerModel(), poseStack, buffers, VisualRenderBridge.profile(), 15728880, false);
    }
}
