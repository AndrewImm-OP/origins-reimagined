package ru.origins_overhaul.client.visual.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ru.origins_overhaul.client.ClientSelectionConfig;
import ru.origins_overhaul.client.visual.modifier.VisualModifier;
import ru.origins_overhaul.client.visual.profile.ResolvedVisualProfile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** A local, screen-space diagnostic emitter. It never inserts particles into a world. */
public final class PreviewParticleSystem {
    private final List<PreviewParticle> particles = new ArrayList<>();
    private float accumulator;
    public void clear() { particles.clear(); accumulator = 0; }
    public void update(float deltaSeconds, ResolvedVisualProfile profile) {
        if (ClientSelectionConfig.reduceMotion() || !ClientSelectionConfig.visualParticles()) { clear(); return; }
        float rate = 0;
        for (VisualModifier modifier : profile.modifiers()) if (modifier.type().getPath().equals("particle_aura")) rate += modifier.particleRate();
        accumulator += Math.min(0.1f, Math.max(0, deltaSeconds)) * rate * ClientSelectionConfig.particleDensity();
        while (accumulator >= 1.0f && particles.size() < 256) { particles.add(new PreviewParticle(ThreadLocalRandom.current().nextFloat(), 0.1f + ThreadLocalRandom.current().nextFloat() * 0.8f, 0.0f, 1.2f + ThreadLocalRandom.current().nextFloat() * 0.8f)); accumulator -= 1.0f; }
        Iterator<PreviewParticle> iterator = particles.iterator();
        while (iterator.hasNext()) { PreviewParticle particle = iterator.next(); particle.age += deltaSeconds; particle.x += particle.vx * deltaSeconds; particle.y += particle.vy * deltaSeconds; if (particle.age >= particle.life) iterator.remove(); }
    }
    public void render(GuiGraphicsExtractor context, int x, int y, int width, int height) {
        for (PreviewParticle particle : particles) { int px = x + Math.round(particle.x * width); int py = y + Math.round((1.0f - particle.y) * height); int alpha = Math.round(255.0f * Math.max(0, 1.0f - particle.age / particle.life)); context.fill(px - 1, py - 1, px + 2, py + 2, alpha << 24 | 0x65D9FF); }
    }
    private static final class PreviewParticle {
        float x, y, age, life; final float vx = (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.08f; final float vy = 0.08f;
        PreviewParticle(float x, float y, float age, float life) { this.x=x; this.y=y; this.age=age; this.life=life; }
    }
}
