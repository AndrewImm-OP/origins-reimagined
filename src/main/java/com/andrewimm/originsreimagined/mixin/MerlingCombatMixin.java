package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.gameplay.MerlingConfig;
import com.andrewimm.originsreimagined.gameplay.MerlingMechanics;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class MerlingCombatMixin {
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean originsReimagined$underwaterDamage(Entity target, DamageSource source, float amount, Operation<Boolean> original) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide() && target instanceof LivingEntity
            && player.isInWater() && OriginsLegacyAdapter.hasOrigin(player, MerlingMechanics.MERLING)) {
            amount *= MerlingConfig.get().underwaterDamageMultiplier;
        }
        return original.call(target, source, amount);
    }
}
