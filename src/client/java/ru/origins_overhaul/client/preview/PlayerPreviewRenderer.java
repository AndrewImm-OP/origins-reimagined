package ru.origins_overhaul.client.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import ru.origins_overhaul.client.visual.context.PreviewPlayerVisualContext;
import ru.origins_overhaul.client.visual.profile.ResolvedVisualProfile;
import ru.origins_overhaul.client.visual.profile.VisualProfileResolver;
import ru.origins_overhaul.client.visual.render.VisualRenderBridge;
import ru.origins_overhaul.client.visual.render.VisualBackendCapabilities;

public final class PlayerPreviewRenderer {
    private PlayerModel wideModel;
    private PlayerModel slimModel;

    public void refreshModels(Minecraft client) {
        EntityModelSet models = client.getEntityModels();
        wideModel = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER), false);
        slimModel = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    public void clear() { wideModel = null; slimModel = null; }

    public boolean render(GuiGraphicsExtractor context, PlayerAppearanceSnapshot appearance, PlayerPreviewCamera camera, int x, int y, int width, int height, float opacity, boolean showOuterLayer, ru.origins_overhaul.client.preview.PreviewOriginContext originContext) {
        if (appearance == null || wideModel == null || slimModel == null || width <= 2 || height <= 2) return false;
        PlayerModel model = appearance.modelType() == net.minecraft.world.entity.player.PlayerModelType.SLIM ? slimModel : wideModel;
        model.hat.visible = showOuterLayer && appearance.showHat();
        model.jacket.visible = showOuterLayer && appearance.showJacket();
        model.leftSleeve.visible = showOuterLayer && appearance.showLeftSleeve();
        model.rightSleeve.visible = showOuterLayer && appearance.showRightSleeve();
        model.leftPants.visible = showOuterLayer && appearance.showLeftPants();
        model.rightPants.visible = showOuterLayer && appearance.showRightPants();
        float scale = 0.97f * height / 2.125f * camera.zoom() * (0.96f + 0.04f * Math.max(0.0f, Math.min(1.0f, opacity)));
        int panX = Math.round(camera.offsetX() * width);
        int panY = Math.round(camera.offsetY() * height);
        int shiftedX = x + panX;
        int shiftedY = y + panY;
        ResolvedVisualProfile visualProfile = VisualProfileResolver.resolve(originContext.visualProfileId(), new PreviewPlayerVisualContext(appearance.playerId(), originContext.originId(), appearance, java.util.Set.of(), 0.0f), true, VisualBackendCapabilities.PREVIEW);
        context.enableScissor(x, y, x + width, y + height);
        try {
            VisualRenderBridge.push(visualProfile);
            VisualRenderBridge.setPreview(visualProfile);
            VisualRenderBridge.setPreviewAppearance(appearance);
            VisualRenderBridge.setPreviewModel(model);
            context.skin(model, appearance.skinTexture(), scale, camera.pitch(), camera.yaw(), -1.0625f, shiftedX, shiftedY, shiftedX + width, shiftedY + height);
            return true;
        } finally {
            VisualRenderBridge.clear();
            context.disableScissor();
        }
    }
}
