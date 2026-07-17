package com.andrewimm.originsreimagined.gameplay;

import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MerlingMechanics {
    public static final Identifier MERLING = Identifier.fromNamespaceAndPath("origins", "merling");
    public static final Identifier UNDERWATER_DAMAGE = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "merling_underwater_damage");
    public static final Identifier FIRE_DAMAGE = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "merling_fire_damage");
    public static final Identifier FORBIDDEN_FOOD = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "merling_forbidden_food");
    public static final Identifier NETHER_DESICCATION = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "merling_nether_desiccation");
    public static final ResourceKey<DamageType> DESICCATION = ResourceKey.create(
        Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "merling_desiccation"));

    private static final Map<UUID, NetherState> NETHER_STATES = new HashMap<>();

    private MerlingMechanics() {}

    public static void register() {
        MerlingConfig.load();
        ServerTickEvents.END_SERVER_TICK.register(MerlingMechanics::tick);
        OriginsReimagined.LOGGER.info("Merling mechanics enabled");
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            if (!OriginsLegacyAdapter.hasOrigin(player, MERLING)
                || !AdminFeatureControl.enabled(player, MERLING, NETHER_DESICCATION)
                || player.isDeadOrDying()) {
                NETHER_STATES.remove(id);
                continue;
            }
            if (player.level().dimension() != Level.NETHER) {
                NETHER_STATES.remove(id);
                continue;
            }

            NetherState state = NETHER_STATES.computeIfAbsent(id, ignored -> new NetherState());
            state.ticksInNether++;
            MerlingConfig config = MerlingConfig.get();
            int warningAt = Math.max(0, config.netherGraceTicks - 40);
            if (!state.warned && state.ticksInNether >= warningAt && state.ticksInNether < config.netherGraceTicks) {
                player.sendOverlayMessage(Component.translatable("origins_reimagined.merling.nether_warning"));
                state.warned = true;
            }
            if (state.ticksInNether <= config.netherGraceTicks) continue;
            if ((state.ticksInNether - config.netherGraceTicks) % config.netherDamageIntervalTicks == 0) {
                player.hurtServer((ServerLevel) player.level(), desiccationSource(player), config.netherDamage);
            }
        }
    }

    private static DamageSource desiccationSource(ServerPlayer player) {
        Holder<DamageType> holder = player.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DESICCATION);
        return new DamageSource(holder);
    }

    private static final class NetherState {
        int ticksInNether;
        boolean warned;
    }
}
