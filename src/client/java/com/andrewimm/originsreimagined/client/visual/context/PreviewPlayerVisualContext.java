package com.andrewimm.originsreimagined.client.visual.context;

import net.minecraft.resources.Identifier;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;

import java.util.Set;
import java.util.UUID;

public record PreviewPlayerVisualContext(UUID playerId, Identifier originId, PlayerAppearanceSnapshot appearance, Set<Identifier> simulatedPowers, float partialTick, boolean inWater, boolean swimming, boolean sneaking, boolean fallFlying, boolean onFire) implements PlayerVisualContext {
    public PreviewPlayerVisualContext(UUID playerId, Identifier originId, PlayerAppearanceSnapshot appearance, Set<Identifier> simulatedPowers, float partialTick) { this(playerId, originId, appearance, simulatedPowers, partialTick, false, false, false, false, false); }
    public PreviewPlayerVisualContext {
        simulatedPowers = Set.copyOf(simulatedPowers == null ? Set.of() : simulatedPowers);
    }
    @Override public boolean preview() { return true; }
    @Override public boolean localPlayer() { return true; }
    @Override public boolean powerActive(Identifier powerId) { return powerId != null && simulatedPowers.contains(powerId); }
}
