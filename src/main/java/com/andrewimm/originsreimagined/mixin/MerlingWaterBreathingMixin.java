package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.gameplay.MerlingMechanics;
import com.andrewimm.originsreimagined.gameplay.AdminFeatureControl;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MerlingWaterBreathingMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void originsReimagined$keepMerlingAir(CallbackInfo callback) {
        Player player = (Player) (Object) this;
        if (OriginsLegacyAdapter.hasOrigin(player, MerlingMechanics.MERLING)
            && AdminFeatureControl.enabled(player, MerlingMechanics.MERLING,
                net.minecraft.resources.Identifier.fromNamespaceAndPath("origins", "water_breathing"))) {
            // Origins Legacy's water_breathing power applies its inverse-air rule
            // in the same tick. Resetting after that hook preserves full air on
            // land and in water while leaving every other origin untouched.
            player.setAirSupply(player.getMaxAirSupply());
        }
    }
}
