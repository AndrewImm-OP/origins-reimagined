package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.gameplay.PhantomMechanics;
import io.github.apace100.apoli.power.BurnPower;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BurnPower.class)
public abstract class PhantomBurnPowerMixin {
    @Shadow protected LivingEntity entity;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void originsReimagined$protectHelmet(CallbackInfo callback) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer player
            && PhantomMechanics.isProtectedFromSun(player)) {
            callback.cancel();
        }
    }
}
