package ru.origins_overhaul.compat.originslegacy;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import ru.origins_overhaul.model.OriginData;
import ru.origins_overhaul.model.OriginLayerSnapshot;
import ru.origins_overhaul.model.PowerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OriginsLegacyAdapter {
    private OriginsLegacyAdapter() {}

    public static List<OriginLayerSnapshot> layers() {
        List<OriginLayer> layers = new ArrayList<>(OriginLayers.getLayers());
        Collections.sort(layers);
        List<OriginLayerSnapshot> result = new ArrayList<>();
        for (int index = 0; index < layers.size(); index++) {
            OriginLayer layer = layers.get(index);
            result.add(new OriginLayerSnapshot(layer.getIdentifier(), index, layer.isEnabled(), layer.isHidden(), layer.isRandomAllowed()));
        }
        return List.copyOf(result);
    }

    public static List<OriginData> origins(OriginLayerSnapshot snapshot, Player player) {
        OriginLayer layer = OriginLayers.getLayer(snapshot.id());
        List<OriginData> result = new ArrayList<>();
        for (Identifier id : layer.getOrigins(player)) {
            if (!OriginRegistry.contains(id)) continue;
            Origin origin = OriginRegistry.get(id);
            result.add(origin(snapshot.id(), origin));
        }
        return List.copyOf(result);
    }

    public static OriginData origin(Identifier layerId, Identifier originId) {
        return origin(layerId, OriginRegistry.get(originId));
    }

    private static OriginData origin(Identifier layerId, Origin origin) {
        List<PowerData> powers = new ArrayList<>();
        for (PowerType<?> type : origin.getPowerTypes()) {
            Identifier powerId = type.getIdentifier();
            if (powerId == null) continue;
            powers.add(new PowerData(powerId, type.getName(), type.getDescription(), type.isHidden()));
        }
        return new OriginData(origin.getIdentifier(), layerId, origin.getName(), origin.getDisplayItem(), origin.getOrder(), origin.getImpact().getImpactValue(), origin.isChoosable(), powers);
    }

    public static boolean contains(Identifier originId) {
        return OriginRegistry.contains(originId);
    }

    public static boolean powerExists(Identifier powerId) {
        return PowerTypeRegistry.contains(powerId);
    }
}
