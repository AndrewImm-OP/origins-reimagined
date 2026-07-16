package com.andrewimm.originsreimagined.mixin;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.andrewimm.originsreimagined.client.visual.render.WorldVisualRenderLayer;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererVisualMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void originsReimagined$addVisualLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo callback) {
        var layer = new WorldVisualRenderLayer((RenderLayerParent<AvatarRenderState, PlayerModel>) this);
        ((LivingEntityRendererAccessor) (Object) this).originsReimagined$getLayers().add(layer);
    }
}
