package ru.origins_overhaul.client.preview;

import net.minecraft.resources.Identifier;

public record PreviewOriginContext(Identifier layerId, Identifier originId, float transitionProgress) {
    public static PreviewOriginContext empty() { return new PreviewOriginContext(null, null, 1.0f); }
}
