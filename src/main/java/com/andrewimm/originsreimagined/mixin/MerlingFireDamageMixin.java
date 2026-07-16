package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.gameplay.MerlingConfig;
import com.andrewimm.originsreimagined.gameplay.MerlingMechanics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class MerlingFireDamageMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float originsReimagined$fireDamage(float amount, ServerLevel level, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (amount > 0.0F && source.is(DamageTypeTags.IS_FIRE)
            && entity instanceof Player player
            && OriginsLegacyAdapter.hasOrigin(player, MerlingMechanics.MERLING)) {
            return amount * MerlingConfig.get().fireDamageMultiplier;
        }
        return amount;
    }
}
