package ru.origins_overhaul.mixin;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.origins_overhaul.client.visual.render.WorldVisualRenderLayer;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererVisualMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void originsOverhaul$addVisualLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo callback) {
        var layer = new WorldVisualRenderLayer((RenderLayerParent<AvatarRenderState, PlayerModel>) this);
        ((LivingEntityRendererAccessor) (Object) this).originsOverhaul$getLayers().add(layer);
    }
}
