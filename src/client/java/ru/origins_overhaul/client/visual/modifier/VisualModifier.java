package ru.origins_overhaul.client.visual.modifier;

import net.minecraft.resources.Identifier;
import ru.origins_overhaul.client.visual.condition.VisualCondition;

import java.util.List;

public record VisualModifier(
    String id,
    Identifier type,
    Identifier texture,
    int color,
    float opacity,
    float strength,
    List<String> parts,
    String anchor,
    VisualCondition condition,
    RenderPhase renderPhase,
    float[] offset,
    float[] rotation,
    float[] scale,
    boolean hideWhenHeadArmor,
    boolean hideWhenChestArmor,
    String geometryType,
    float[] geometrySize,
    int[] uv,
    boolean mirror,
    String animationType,
    float animationAmplitude,
    float animationSpeed,
    float animationWalkMultiplier
    ,
    Identifier particle,
    float particleRate,
    float particleRadius,
    float particleHeight
) {
    public VisualModifier(Identifier type, Identifier texture, int color, float opacity, float strength, List<String> parts,
                          String anchor, VisualCondition condition, RenderPhase renderPhase, float[] offset, float[] rotation,
                          float[] scale, boolean hideWhenHeadArmor, boolean hideWhenChestArmor) {
        this(type.getPath(), type, texture, color, opacity, strength, parts, anchor, condition, renderPhase, offset, rotation, scale,
            hideWhenHeadArmor, hideWhenChestArmor, "cuboid", new float[]{1, 1, 1}, new int[]{0, 0}, false, "STATIC", 0, 0, 0, null, 0, 0.45f, 1.7f);
    }
    public VisualModifier {
        id = id == null || id.isBlank() ? type.getPath() : id;
        parts = List.copyOf(parts == null ? List.of("full_model") : parts);
        condition = condition == null ? VisualCondition.always() : condition;
        renderPhase = renderPhase == null ? RenderPhase.AFTER_OUTER_LAYER : renderPhase;
        offset = clean(offset, new float[]{0.0f, 0.0f, 0.0f});
        rotation = clean(rotation, new float[]{0.0f, 0.0f, 0.0f});
        scale = clean(scale, new float[]{1.0f, 1.0f, 1.0f});
        geometryType = geometryType == null ? "cuboid" : geometryType.toLowerCase(java.util.Locale.ROOT);
        geometrySize = clean(geometrySize, new float[]{1.0f, 1.0f, 1.0f});
        uv = uv == null || uv.length != 2 ? new int[]{0, 0} : uv.clone();
        animationType = animationType == null ? "STATIC" : animationType.toUpperCase(java.util.Locale.ROOT);
        animationAmplitude = Math.max(0, finite(animationAmplitude, 0));
        animationSpeed = Math.max(0, finite(animationSpeed, 0));
        animationWalkMultiplier = Math.max(0, finite(animationWalkMultiplier, 0));
        particleRate = Math.max(0, finite(particleRate, 0));
        particleRadius = Math.max(0, finite(particleRadius, 0.45f));
        particleHeight = Math.max(0, finite(particleHeight, 1.7f));
    }
    private static float[] clean(float[] source, float[] fallback) {
        if (source == null || source.length != 3) return fallback;
        return source.clone();
    }
    private static float finite(float value, float fallback) { return Float.isFinite(value) ? value : fallback; }
}
