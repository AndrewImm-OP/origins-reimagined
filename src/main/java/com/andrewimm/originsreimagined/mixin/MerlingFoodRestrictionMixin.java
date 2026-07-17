package com.andrewimm.originsreimagined.mixin;

import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.gameplay.MerlingMechanics;
import com.andrewimm.originsreimagined.gameplay.AdminFeatureControl;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MerlingFoodRestrictionMixin {
    private static final TagKey<net.minecraft.world.item.Item> FORBIDDEN_FOOD = TagKey.create(
        Registries.ITEM, Identifier.fromNamespaceAndPath("origins_reimagined", "merling_forbidden_food"));

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void originsReimagined$rejectFish(Level level, Player player, InteractionHand hand,
                                               CallbackInfoReturnable<InteractionResult> callback) {
        if (player != null && ((ItemStack) (Object) this).is(FORBIDDEN_FOOD)
            && OriginsLegacyAdapter.hasOrigin(player, MerlingMechanics.MERLING)) {
            if (!AdminFeatureControl.enabled(player, MerlingMechanics.MERLING, MerlingMechanics.FORBIDDEN_FOOD)) return;
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("origins_reimagined.merling.forbidden_food"));
            }
            callback.setReturnValue(InteractionResult.FAIL);
        }
    }
}
