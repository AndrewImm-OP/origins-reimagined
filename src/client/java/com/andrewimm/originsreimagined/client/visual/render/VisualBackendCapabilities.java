package com.andrewimm.originsreimagined.client.visual.render;

import java.util.EnumSet;
import java.util.Set;

public final class VisualBackendCapabilities {
    public static final Set<VisualRenderCapability> PREVIEW = Set.of(
        VisualRenderCapability.MODEL_TINT,
        VisualRenderCapability.TEXTURE_OVERLAY,
        VisualRenderCapability.EMISSIVE_OVERLAY,
        VisualRenderCapability.GEOMETRY_ATTACHMENT
    );
    public static final Set<VisualRenderCapability> WORLD = Set.of(
        VisualRenderCapability.MODEL_TINT,
        VisualRenderCapability.TEXTURE_OVERLAY,
        VisualRenderCapability.EMISSIVE_OVERLAY,
        VisualRenderCapability.GEOMETRY_ATTACHMENT,
        VisualRenderCapability.PARTICLE_AURA
    );
    private VisualBackendCapabilities() {}
    public static Set<VisualRenderCapability> copy(Set<VisualRenderCapability> source) {
        return source == null || source.isEmpty() ? EnumSet.noneOf(VisualRenderCapability.class) : EnumSet.copyOf(source);
    }
}
