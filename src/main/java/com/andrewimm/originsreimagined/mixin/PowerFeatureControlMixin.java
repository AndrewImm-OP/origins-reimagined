package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.gameplay.AdminFeatureControl;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Power.class)
public abstract class PowerFeatureControlMixin {
    @Shadow protected LivingEntity entity;
    @Shadow protected PowerType<?> type;

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void originsReimagined$featureGate(CallbackInfoReturnable<Boolean> callback) {
        if (!AdminFeatureControl.powerEnabled(entity, type)) callback.setReturnValue(false);
    }
}
