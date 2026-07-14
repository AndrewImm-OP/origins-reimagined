package ru.origins_overhaul.client.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import ru.origins_overhaul.client.ClientSelectionConfig;

public final class PlayerPreviewController {
    private static long modelRevision;
    private final PlayerPreviewCamera camera = new PlayerPreviewCamera();
    private final PlayerPreviewInputHandler input = new PlayerPreviewInputHandler();
    private final PlayerPreviewRenderer renderer = new PlayerPreviewRenderer();
    private final PlayerPreviewState state = new PlayerPreviewState();
    private float refreshCooldown;
    private long observedModelRevision = -1L;

    public void initialize(Minecraft client) {
        renderer.refreshModels(client);
        observedModelRevision = modelRevision;
        refreshAppearance(client);
        state.showOuterLayer(ClientSelectionConfig.previewShowOuterLayer());
        state.showCape(ClientSelectionConfig.previewShowCape());
        state.showEquipment(ClientSelectionConfig.previewShowEquipment());
    }

    public void update(float deltaSeconds, Minecraft client) {
        if (observedModelRevision != modelRevision) {
            renderer.refreshModels(client);
            observedModelRevision = modelRevision;
        }
        camera.update(deltaSeconds, ClientSelectionConfig.previewAutoRotate(), ClientSelectionConfig.previewAutoRotateSpeed(), ClientSelectionConfig.reduceMotion());
        refreshCooldown -= deltaSeconds;
        if (refreshCooldown <= 0.0f) { refreshCooldown = 0.5f; refreshAppearance(client); }
    }

    public boolean render(GuiGraphicsExtractor context, int x, int y, int width, int height, float opacity) {
        if (!ClientSelectionConfig.previewEnabled()) return false;
        return renderer.render(context, state.appearance(), camera, x, y, width, height, opacity, state.showOuterLayer());
    }

    public void refreshAppearance(Minecraft client) {
        if (client.player == null) { state.clear(); return; }
        if (state.appearance() == null || !state.appearance().playerId().equals(client.player.getUUID()) || !state.appearance().skin().equals(client.player.getSkin())) state.appearance(PlayerAppearanceSnapshot.from(client.player, client.options));
    }

    public void setOriginContext(Identifier layerId, Identifier originId, float transitionProgress) { state.originContext(new PreviewOriginContext(layerId, originId, transitionProgress)); }
    public PlayerPreviewCamera camera() { return camera; }
    public PlayerPreviewInputHandler input() { return input; }
    public PlayerPreviewState state() { return state; }
    public void reset() { camera.reset(); }
    public void clear() { state.clear(); renderer.clear(); input.release(); }
    public static void invalidateModels() { modelRevision++; }
}
