package com.andrewimm.originsreimagined.gameplay;

import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Server-side Phantom sunlight protection and helmet wear. */
public final class PhantomMechanics {
    public static final Identifier PHANTOM = Identifier.fromNamespaceAndPath("origins", "phantom");

    public static final int SUNLIGHT_WEAR_INTERVAL_TICKS = 1_200;
    public static final int SUNLIGHT_WEAR_AMOUNT = 5;

    private static final Map<UUID, Integer> EXPOSURE_TICKS = new HashMap<>();

    private PhantomMechanics() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PhantomMechanics::tick);
        OriginsReimagined.LOGGER.info("Phantom sunlight mechanics enabled");
    }

    public static boolean isProtectedFromSun(ServerPlayer player) {
        return OriginsLegacyAdapter.hasOrigin(player, PHANTOM)
            && isExposedToSun(player)
            && !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
    }

    private static void tick(MinecraftServer server) {
        Set<UUID> online = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            online.add(id);

            if (!OriginsLegacyAdapter.hasOrigin(player, PHANTOM)
                || player.isDeadOrDying()
                || !isExposedToSun(player)
                || player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                EXPOSURE_TICKS.remove(id);
                continue;
            }

            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isDamageableItem()) {
                EXPOSURE_TICKS.remove(id);
                continue;
            }

            int ticks = EXPOSURE_TICKS.merge(id, 1, Integer::sum);
            if (ticks >= SUNLIGHT_WEAR_INTERVAL_TICKS) {
                damageHelmetIgnoringEnchantments(player, helmet);
                EXPOSURE_TICKS.put(id, 0);
            }
        }
        EXPOSURE_TICKS.keySet().removeIf(id -> !online.contains(id));
    }

    private static boolean isExposedToSun(ServerPlayer player) {
        if (!player.level().isBrightOutside()) return false;
        if (player.getLightLevelDependentMagicValue() <= 0.5F) return false;

        BlockPos pos = BlockPos.containing(player.getX(), Math.round(player.getY()), player.getZ());
        return player.level().canSeeSky(pos) && !player.level().isRainingAt(pos);
    }

    private static void damageHelmetIgnoringEnchantments(ServerPlayer player, ItemStack helmet) {
        int newDamage = helmet.getDamageValue() + SUNLIGHT_WEAR_AMOUNT;
        if (newDamage < helmet.getMaxDamage()) {
            helmet.setDamageValue(newDamage);
            return;
        }

        player.onEquippedItemBroken(helmet.getItem(), EquipmentSlot.HEAD);
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    }
}
