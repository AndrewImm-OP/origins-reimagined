package ru.origins_overhaul.client.visual.render;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import ru.origins_overhaul.OriginsOverhaul;
import ru.origins_overhaul.client.ClientSelectionConfig;
import ru.origins_overhaul.client.preview.PlayerAppearanceSnapshot;
import ru.origins_overhaul.client.visual.context.WorldPlayerVisualContext;
import ru.origins_overhaul.client.visual.modifier.VisualModifier;
import ru.origins_overhaul.client.visual.profile.ResolvedVisualProfile;
import ru.origins_overhaul.client.visual.profile.VisualProfileResolver;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;

public final class ParticleAuraManager {
    private static final Map<String, Float> ACCUMULATORS = new ConcurrentHashMap<>();
    private ParticleAuraManager() {}
    public static void register() { ClientTickEvents.END_CLIENT_TICK.register(ParticleAuraManager::tick); }
    public static void clear() { ACCUMULATORS.clear(); }
    private static void tick(Minecraft client) {
        if (!ClientSelectionConfig.visualsEnabled() || !ClientSelectionConfig.visualParticles() || client.level == null || client.player == null) { clear(); return; }
        for (AbstractClientPlayer player : client.level.players()) {
            if (player != client.player && !ClientSelectionConfig.showOtherParticles()) continue;
            if (player.distanceToSqr(client.player) > ClientSelectionConfig.particleDistance() * ClientSelectionConfig.particleDistance()) continue;
            PlayerAppearanceSnapshot appearance = PlayerAppearanceSnapshot.from(player, client.options);
            ResolvedVisualProfile profile = VisualProfileResolver.resolve(new WorldPlayerVisualContext(player, appearance, 0), true, VisualBackendCapabilities.WORLD);
            for (VisualModifier modifier : profile.modifiers()) {
                if (!modifier.type().getPath().equals("particle_aura") || modifier.particle() == null) continue;
                String key = player.getUUID() + ":" + modifier.id();
                float accumulator = ACCUMULATORS.getOrDefault(key, 0.0f) + modifier.particleRate() * ClientSelectionConfig.particleDensity();
                int count = Math.min(3, (int) accumulator);
                accumulator -= count;
                ACCUMULATORS.put(key, accumulator);
                SimpleParticleType type = particle(modifier.particle());
                if (type == null) continue;
                for (int i = 0; i < count; i++) {
                    var random = ThreadLocalRandom.current();
                    double angle = random.nextDouble() * Math.PI * 2;
                    double radius = random.nextDouble() * modifier.particleRadius();
                    client.level.addParticle(type, player.getX() + Math.cos(angle) * radius, player.getY() + random.nextDouble() * modifier.particleHeight(), player.getZ() + Math.sin(angle) * radius, 0, 0.01, 0);
                }
            }
        }
    }
    private static SimpleParticleType particle(Identifier id) {
        if (id.equals(Identifier.parse("minecraft:soul"))) return ParticleTypes.SOUL;
        if (id.equals(Identifier.parse("minecraft:bubble"))) return ParticleTypes.BUBBLE;
        if (id.equals(Identifier.parse("minecraft:flame"))) return ParticleTypes.FLAME;
        return null;
    }
}
