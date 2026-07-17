package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.gameplay.FelineMechanics;
import com.andrewimm.originsreimagined.gameplay.MerlingConfig;
import com.andrewimm.originsreimagined.gameplay.AdminFeatureControl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class FelineFoodBonusMixin {
    private float originsReimagined$beforeSaturation;

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void originsReimagined$captureFood(Level level, LivingEntity user, CallbackInfoReturnable<ItemStack> callback) {
        originsReimagined$beforeSaturation = user instanceof Player player ? player.getFoodData().getSaturationLevel() : 0.0F;
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void originsReimagined$foodBonus(Level level, LivingEntity user, CallbackInfoReturnable<ItemStack> callback) {
        if (level.isClientSide() || !(user instanceof Player player)
            || !OriginsLegacyAdapter.hasOrigin(player, FelineMechanics.FELINE)
            || !AdminFeatureControl.enabled(player, FelineMechanics.FELINE, FelineMechanics.FOOD)) return;
        ItemStack stack = (ItemStack) (Object) this;
        if (!stack.is(net.minecraft.tags.ItemTags.MEAT) && !stack.is(net.minecraft.tags.ItemTags.FISHES)) return;
        var food = stack.get(DataComponents.FOOD);
        if (food == null) return;
        float delta = Math.max(0.0F, player.getFoodData().getSaturationLevel() - originsReimagined$beforeSaturation);
        player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel()
            + delta * (MerlingConfig.get().felineFoodSaturationMultiplier - 1.0F));
    }
}
