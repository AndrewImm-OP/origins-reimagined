package io.github.apace100.origins.screen;

import net.minecraft.client.Minecraft;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import ru.origins_overhaul.client.OriginSelectionSession;
import ru.origins_overhaul.client.screen.CinematicOriginSelectionScreen;

import java.util.ArrayList;

public final class OriginsOverhaulWaitForNextLayerScreen extends WaitForNextLayerScreen {
    private final OriginSelectionSession session;

    public OriginsOverhaulWaitForNextLayerScreen(OriginSelectionSession session) {
        super(upstreamLayers(session), 0, false);
        this.session = session;
    }

    private static ArrayList<OriginLayer> upstreamLayers(OriginSelectionSession session) {
        ArrayList<OriginLayer> result = new ArrayList<>();
        for (var id : session.layers()) result.add(OriginLayers.getLayer(id));
        return result;
    }

    @Override
    public void openSelection() {
        if (Minecraft.getInstance().player != null && session.advanceToNextLayer(Minecraft.getInstance().player)) {
            Minecraft.getInstance().setScreen(new CinematicOriginSelectionScreen(session));
        } else {
            Minecraft.getInstance().setScreen(null);
        }
    }
}
