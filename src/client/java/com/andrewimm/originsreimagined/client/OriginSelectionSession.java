package com.andrewimm.originsreimagined.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.model.OriginLayerSnapshot;
import com.andrewimm.originsreimagined.model.OriginPresentation;

import java.util.List;

public final class OriginSelectionSession {
    private final List<Identifier> layers;
    private final boolean showDirtBackground;
    private int currentLayerIndex;
    private int selectedOriginIndex;
    private boolean selectionSubmitted;
    private int lastDirection;

    private OriginSelectionSession(List<Identifier> layers, boolean showDirtBackground, int currentLayerIndex) {
        this.layers = List.copyOf(layers);
        this.showDirtBackground = showDirtBackground;
        this.currentLayerIndex = currentLayerIndex;
    }

    public static OriginSelectionSession fromServer(Player player, boolean showDirtBackground) {
        List<Identifier> layers = OriginsLegacyAdapter.selectableLayers(player).stream().map(OriginLayerSnapshot::id).toList();
        return new OriginSelectionSession(layers, showDirtBackground, 0);
    }

    public List<Identifier> layers() { return layers; }
    public boolean showDirtBackground() { return showDirtBackground; }
    public int currentLayerIndex() { return currentLayerIndex; }
    public Identifier currentLayerId() { return layers.get(currentLayerIndex); }
    public List<OriginPresentation> currentOrigins() {
        if (!hasCurrentLayer() || com.andrewimm.originsreimagined.client.ClientOriginCatalog.getLayers().stream().noneMatch(l -> l.id().equals(currentLayerId()))) return List.of();
        var snapshot = com.andrewimm.originsreimagined.client.ClientOriginCatalog.getLayers().stream().filter(l -> l.id().equals(currentLayerId())).findFirst().orElse(null);
        if (snapshot == null || MinecraftPlayerHolder.player() == null) return ClientOriginCatalog.getOrigins(currentLayerId());
        var allowed = OriginsLegacyAdapter.choosableOrigins(snapshot, MinecraftPlayerHolder.player()).stream().map(d -> d.id()).toList();
        return ClientOriginCatalog.getOrigins(currentLayerId()).stream().filter(o -> allowed.contains(o.originId())).toList();
    }
    public int selectedOriginIndex() { return selectedOriginIndex; }
    public boolean selectionSubmitted() { return selectionSubmitted; }
    public int lastDirection() { return lastDirection; }
    public boolean hasCurrentLayer() { return !layers.isEmpty() && currentLayerIndex < layers.size(); }
    public boolean randomAllowed(Player player) { return hasCurrentLayer() && OriginsLegacyAdapter.hasRandomChoice(currentLayerId(), player); }

    public OriginPresentation selectedOrigin() {
        List<OriginPresentation> origins = currentOrigins();
        return origins.isEmpty() ? null : origins.get(Math.min(selectedOriginIndex, origins.size() - 1));
    }

    public void select(int index) {
        if (!currentOrigins().isEmpty()) selectedOriginIndex = Math.floorMod(index, currentOrigins().size());
    }

    public void move(int direction) {
        List<OriginPresentation> origins = currentOrigins();
        if (origins.size() > 1) {
            selectedOriginIndex = Math.floorMod(selectedOriginIndex + direction, origins.size());
            lastDirection = Integer.signum(direction);
        }
    }

    public void resetForCurrentLayer() { selectedOriginIndex = 0; selectionSubmitted = false; lastDirection = 0; }
    public void markSubmitted() { selectionSubmitted = true; }

    public boolean advanceToNextLayer(Player player) {
        int next = currentLayerIndex + 1;
        while (next < layers.size()) {
            Identifier nextLayerId = layers.get(next);
            OriginLayerSnapshot snapshot = OriginsLegacyAdapter.layers().stream().filter(l -> l.id().equals(nextLayerId)).findFirst().orElse(null);
            if (snapshot != null && (!OriginsLegacyAdapter.choosableOrigins(snapshot, player).isEmpty() || OriginsLegacyAdapter.hasRandomChoice(nextLayerId, player))) {
                currentLayerIndex = next;
                resetForCurrentLayer();
                return true;
            }
            next++;
        }
        return false;
    }

    private static final class MinecraftPlayerHolder {
        private static Player player() { return net.minecraft.client.Minecraft.getInstance().player; }
    }
}
