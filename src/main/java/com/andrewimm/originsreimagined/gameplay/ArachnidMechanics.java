package com.andrewimm.originsreimagined.gameplay;

import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.networking.StickyThreadsPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative Arachnid web ability and web movement bonuses. */
public final class ArachnidMechanics {
    public static final Identifier ARACHNID = Identifier.fromNamespaceAndPath("origins", "arachnid");
    public static final Identifier STICKY_THREADS = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "arachnid_sticky_threads");
    public static final Identifier SPIDER_SPEED = Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "arachnid_web_speed");
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final Map<BlockPos, WebTrap> WEBS = new HashMap<>();

    private ArachnidMechanics() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(StickyThreadsPayload.TYPE, (payload, context) ->
            context.server().execute(() -> fire(context.player())));
        ServerTickEvents.END_SERVER_TICK.register(ArachnidMechanics::tick);
        OriginsReimagined.LOGGER.info("Arachnid mechanics enabled");
    }

    private static void fire(ServerPlayer player) {
        if (!OriginsLegacyAdapter.hasOrigin(player, ARACHNID)
            || !AdminFeatureControl.enabled(player, ARACHNID, STICKY_THREADS)) return;
        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        COOLDOWNS.put(player.getUUID(), now + MerlingConfig.get().arachnidStickyCooldownTicks);

        HitResult hit = ProjectileUtil.getHitResultOnViewVector(player,
            entity -> entity instanceof LivingEntity && entity != player && entity.isPickable(), 32.0D);
        if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) return;
        BlockPos pos = target.blockPosition();
        if (!level.getBlockState(pos).canBeReplaced()) return;
        level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
        WEBS.put(pos.immutable(), new WebTrap(level, pos.immutable(), target,
            MerlingConfig.get().arachnidWebDurationTicks));
    }

    private static void tick(net.minecraft.server.MinecraftServer server) {
        Iterator<Map.Entry<BlockPos, WebTrap>> iterator = WEBS.entrySet().iterator();
        while (iterator.hasNext()) {
            WebTrap trap = iterator.next().getValue();
            trap.ticks--;
            if (trap.ticks <= 0 || !trap.level.getBlockState(trap.pos).is(Blocks.COBWEB)) {
                if (trap.level.getBlockState(trap.pos).is(Blocks.COBWEB)) trap.level.removeBlock(trap.pos, false);
                applySlow(trap.target);
                iterator.remove();
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean arachnid = OriginsLegacyAdapter.hasOrigin(player, ARACHNID);
            boolean nearTarget = arachnid && WEBS.values().stream().anyMatch(web ->
                web.target != player && web.target.isAlive() && web.target.distanceToSqr(player) <= 64.0D);
            var attributes = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributes == null) continue;
            if (nearTarget && AdminFeatureControl.enabled(player, ARACHNID, SPIDER_SPEED)) {
                attributes.addOrUpdateTransientModifier(new AttributeModifier(SPIDER_SPEED,
                    MerlingConfig.get().arachnidSpeedMultiplier - 1.0D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else if (attributes.hasModifier(Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "arachnid_web_speed"))) {
                attributes.removeModifier(Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "arachnid_web_speed"));
            }
        }
    }

    private static void applySlow(LivingEntity target) {
        Holder<net.minecraft.world.effect.MobEffect> effect = MobEffects.SLOWNESS;
        target.addEffect(new MobEffectInstance(effect, MerlingConfig.get().arachnidSlowDurationTicks, 0, false, true, true));
    }

    private static final class WebTrap {
        final ServerLevel level;
        final BlockPos pos;
        final LivingEntity target;
        int ticks;
        WebTrap(ServerLevel level, BlockPos pos, LivingEntity target, int ticks) {
            this.level = level; this.pos = pos; this.target = target; this.ticks = ticks;
        }
    }
}
