package com.andrewimm.originsreimagined.gameplay;

import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-side Feline desert neutrality, wet wool and food tuning. */
public final class FelineMechanics {
    public static final Identifier FELINE = Identifier.fromNamespaceAndPath("origins", "feline");
    public static final Identifier DESERT_NEUTRALITY = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "feline_desert_neutrality");
    public static final Identifier WET_WOOL = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "feline_wet_wool");
    public static final Identifier FOOD = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "feline_food_bonus");
    public static final TagKey<net.minecraft.world.level.biome.Biome> DESERT_BIOMES = TagKey.create(
        Registries.BIOME, Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "desert_biomes"));
    private static final UUID WET_UUID = UUID.fromString("4cfbc5f5-55d9-4c45-9f23-6bbd2ff25a24");
    private static final Map<UUID, Long> WET_UNTIL = new HashMap<>();
    private static final Map<UUID, Map<UUID, Long>> PROVOKED = new HashMap<>();

    private FelineMechanics() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && entity instanceof Mob mob
                && OriginsLegacyAdapter.hasOrigin(serverPlayer, FELINE)) {
                PROVOKED.computeIfAbsent(mob.getUUID(), ignored -> new HashMap<>())
                    .put(serverPlayer.getUUID(), serverPlayer.level().getGameTime() + 1200L);
            }
            return InteractionResult.PASS;
        });
        ServerTickEvents.END_SERVER_TICK.register(FelineMechanics::tick);
        OriginsReimagined.LOGGER.info("Feline mechanics enabled");
    }

    public static boolean shouldBlockTarget(Mob mob, LivingEntity target) {
        if (!(target instanceof Player player) || !OriginsLegacyAdapter.hasOrigin(player, FELINE)
            || !AdminFeatureControl.enabled(player, FELINE, DESERT_NEUTRALITY)
            || !isDesert(player)) return false;
        return !isProvoked(mob, player);
    }

    public static boolean wetPenaltyActive(Player player) {
        if (player == null || player.getVehicle() instanceof AbstractBoat) return false;
        long now = player.level().getGameTime();
        return player.isInWater() || WET_UNTIL.getOrDefault(player.getUUID(), 0L) > now;
    }

    private static boolean isDesert(Player player) {
        return player.level().getBiome(BlockPos.containing(player.position())).is(DESERT_BIOMES);
    }

    private static boolean isProvoked(Mob mob, Player player) {
        return PROVOKED.getOrDefault(mob.getUUID(), Map.of()).getOrDefault(player.getUUID(), 0L)
            >= mob.level().getGameTime();
    }

    private static void tick(net.minecraft.server.MinecraftServer server) {
        long now = server.overworld().getGameTime();
        PROVOKED.values().forEach(map -> map.entrySet().removeIf(entry -> entry.getValue() < now));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!OriginsLegacyAdapter.hasOrigin(player, FELINE)
                || !AdminFeatureControl.enabled(player, FELINE, WET_WOOL)
                || player.getVehicle() instanceof AbstractBoat) {
                removeWet(player);
                continue;
            }
            boolean wet = player.isInWater();
            if (wet) WET_UNTIL.put(player.getUUID(), now + MerlingConfig.get().felineWetGraceTicks);
            boolean slowed = wet || WET_UNTIL.getOrDefault(player.getUUID(), 0L) > now;
            var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attribute == null) continue;
            if (slowed) attribute.addOrUpdateTransientModifier(new AttributeModifier(
                Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "feline_wet_wool"),
                MerlingConfig.get().felineWetSpeedMultiplier - 1.0D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            else removeWet(player);
        }
    }

    private static void removeWet(Player player) {
        WET_UNTIL.remove(player.getUUID());
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) attribute.removeModifier(Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "feline_wet_wool"));
    }
}
