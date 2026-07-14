package ru.origins_overhaul.client;

import ru.origins_overhaul.client.visual.anchor.EyeAnchor;
import ru.origins_overhaul.client.visual.anchor.EyeLayer;
import ru.origins_overhaul.client.visual.anchor.EyePreset;
import ru.origins_overhaul.client.visual.modifier.RenderPhase;
import ru.origins_overhaul.client.visual.render.VisualBackendCapabilities;
import ru.origins_overhaul.client.visual.render.VisualRenderCapability;

/** Small JVM-only checks for visual data that do not require a Minecraft window. */
public final class VisualMathSelfTest {
    public static void main(String[] args) {
        EyeAnchor clamped = new EyeAnchor(-4, 12, 99, -2, EyeLayer.BOTH);
        assert clamped.x() == 0 && clamped.y() == 7;
        assert clamped.width() == 8 && clamped.height() == 0;
        assert EyePreset.STANDARD.left().width() > 0;
        assert EyePreset.STANDARD.right().width() > 0;
        assert RenderPhase.values().length == 4;
        assert VisualBackendCapabilities.WORLD.contains(VisualRenderCapability.GEOMETRY_ATTACHMENT);
        assert !VisualBackendCapabilities.PREVIEW.contains(VisualRenderCapability.MODEL_ALPHA);
        System.out.println("VisualMathSelfTest passed");
    }
}
