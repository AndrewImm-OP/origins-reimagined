package ru.origins_overhaul.client.preview;

import net.minecraft.resources.Identifier;
import java.util.Set;

public record PreviewOriginContext(Identifier layerId, Identifier originId, float transitionProgress, Identifier visualProfileId, Set<Identifier> simulatedPowers, boolean inWater, boolean swimming, boolean sneaking, boolean fallFlying, boolean onFire, String modelOverride) {
    public PreviewOriginContext(Identifier layerId, Identifier originId, float transitionProgress) { this(layerId, originId, transitionProgress, originId, Set.of(), false, false, false, false, false, "AUTO"); }
    public PreviewOriginContext(Identifier layerId, Identifier originId, float transitionProgress, Identifier visualProfileId) { this(layerId, originId, transitionProgress, visualProfileId, Set.of(), false, false, false, false, false, "AUTO"); }
    public PreviewOriginContext { simulatedPowers = Set.copyOf(simulatedPowers == null ? Set.of() : simulatedPowers); modelOverride = modelOverride == null ? "AUTO" : modelOverride; }
    public static PreviewOriginContext empty() { return new PreviewOriginContext(null, null, 1.0f, null); }
}
