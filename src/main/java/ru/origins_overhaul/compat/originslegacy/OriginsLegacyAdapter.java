package ru.origins_overhaul.compat.originslegacy;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.registry.ModComponents;
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

    public static List<OriginData> choosableOrigins(OriginLayerSnapshot snapshot, Player player) {
        return origins(snapshot, player).stream().filter(OriginData::choosable).toList();
    }

    public static List<OriginLayerSnapshot> selectableLayers(Player player) {
        OriginComponent component = ModComponents.ORIGIN.get(player);
        return layers().stream()
            .filter(layer -> layer.enabled() && !component.hasOrigin(OriginLayers.getLayer(layer.id())))
            .toList();
    }

    public static boolean hasRandomChoice(Identifier layerId, Player player) {
        OriginLayer layer = OriginLayers.getLayer(layerId);
        return layer.isRandomAllowed() && !layer.getRandomOrigins(player).isEmpty();
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

    public static boolean hasOrigin(Player player, Identifier originId) {
        if (player == null || originId == null) return false;
        return ModComponents.ORIGIN.get(player).getOrigins().values().stream()
            .anyMatch(origin -> originId.equals(origin.getIdentifier()));
    }
}
