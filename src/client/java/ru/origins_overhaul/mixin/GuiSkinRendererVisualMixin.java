package ru.origins_overhaul.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.render.pip.GuiSkinRenderer;
import net.minecraft.client.model.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.origins_overhaul.client.visual.render.VisualRenderBridge;

@Mixin(GuiSkinRenderer.class)
public abstract class GuiSkinRendererVisualMixin {
    @WrapOperation(method = "renderToTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void originsOverhaul$applyPreviewTint(Model<?> model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, Operation<Void> original) {
        int tint = VisualRenderBridge.tint();
        if (tint == 0xFFFFFFFF) original.call(model, poseStack, consumer, light, overlay);
        else model.renderToBuffer(poseStack, consumer, light, overlay, tint);
    }
}
