package ru.origins_overhaul.client.visual.modifier;

import net.minecraft.resources.Identifier;
import ru.origins_overhaul.client.visual.condition.VisualCondition;

import java.util.List;

public record VisualModifier(
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
    boolean hideWhenChestArmor
) {
    public VisualModifier {
        parts = List.copyOf(parts == null ? List.of("full_model") : parts);
        condition = condition == null ? VisualCondition.always() : condition;
        renderPhase = renderPhase == null ? RenderPhase.AFTER_OUTER_LAYER : renderPhase;
        offset = clean(offset, new float[]{0.0f, 0.0f, 0.0f});
        rotation = clean(rotation, new float[]{0.0f, 0.0f, 0.0f});
        scale = clean(scale, new float[]{1.0f, 1.0f, 1.0f});
    }
    private static float[] clean(float[] source, float[] fallback) {
        if (source == null || source.length != 3) return fallback;
        return source.clone();
    }
}
