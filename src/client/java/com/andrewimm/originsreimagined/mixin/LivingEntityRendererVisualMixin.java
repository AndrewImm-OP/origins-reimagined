package com.andrewimm.originsreimagined.mixin;

import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;
import com.andrewimm.originsreimagined.client.visual.context.WorldPlayerVisualContext;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;
import com.andrewimm.originsreimagined.client.visual.profile.ResolvedVisualProfile;
import com.andrewimm.originsreimagined.client.visual.profile.VisualProfileResolver;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererVisualMixin {
    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true)
    private void originsOverhaul$applyModelTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> callback) {
        if (!(state instanceof AvatarRenderState avatar) || Minecraft.getInstance().level == null) return;
        if (!(Minecraft.getInstance().level.getEntity(avatar.id) instanceof AbstractClientPlayer player)) return;
        PlayerAppearanceSnapshot appearance = PlayerAppearanceSnapshot.from(player, Minecraft.getInstance().options);
        ResolvedVisualProfile profile = VisualProfileResolver.resolve(new WorldPlayerVisualContext(player, appearance, 0.0f), true);
        int tint = 0xFFFFFFFF;
        boolean changed = false;
        for (VisualModifier modifier : profile.modifiers()) {
            if (modifier.type().getPath().equals("model_tint")) {
                tint = ARGB.multiply(tint, modifier.color());
                changed = true;
            }
        }
        if (changed) callback.setReturnValue(tint);
    }
}
