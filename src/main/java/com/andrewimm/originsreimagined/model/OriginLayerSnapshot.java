package com.andrewimm.originsreimagined.model;

import net.minecraft.resources.Identifier;

public record OriginLayerSnapshot(
        Identifier id,
        int order,
        boolean enabled,
        boolean hidden,
        boolean randomAllowed
) {}
