package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.gameplay.FelineMechanics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class FelineMobTargetMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void originsReimagined$desertNeutrality(LivingEntity target, CallbackInfo callback) {
        Mob mob = (Mob) (Object) this;
        if (target != null && mob.getTarget() != target && FelineMechanics.shouldBlockTarget(mob, target)) {
            callback.cancel();
        }
    }
}
