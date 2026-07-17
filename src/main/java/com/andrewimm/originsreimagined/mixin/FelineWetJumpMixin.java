package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.gameplay.AdminFeatureControl;
import com.andrewimm.originsreimagined.gameplay.FelineMechanics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class FelineWetJumpMixin {
    @Inject(method = "getJumpPower", at = @At("HEAD"), cancellable = true)
    private void originsReimagined$disableSprintJumpInWater(CallbackInfoReturnable<Float> callback) {
        Player player = (Player) (Object) this;
        if (player.isSprinting() && OriginsLegacyAdapter.hasOrigin(player, FelineMechanics.FELINE)
            && AdminFeatureControl.enabled(player, FelineMechanics.FELINE, FelineMechanics.WET_WOOL)
            && FelineMechanics.wetPenaltyActive(player)) {
            callback.setReturnValue(Player.BASE_JUMP_POWER);
        }
    }
}
