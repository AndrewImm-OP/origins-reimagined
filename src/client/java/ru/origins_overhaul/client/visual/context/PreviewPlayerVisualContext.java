package ru.origins_overhaul.client.visual.context;

import net.minecraft.resources.Identifier;
import ru.origins_overhaul.client.preview.PlayerAppearanceSnapshot;

import java.util.Set;
import java.util.UUID;

public record PreviewPlayerVisualContext(UUID playerId, Identifier originId, PlayerAppearanceSnapshot appearance, Set<Identifier> simulatedPowers, float partialTick) implements PlayerVisualContext {
    public PreviewPlayerVisualContext {
        simulatedPowers = Set.copyOf(simulatedPowers == null ? Set.of() : simulatedPowers);
    }
    @Override public boolean preview() { return true; }
    @Override public boolean localPlayer() { return true; }
    @Override public boolean powerActive(Identifier powerId) { return powerId != null && simulatedPowers.contains(powerId); }
}
