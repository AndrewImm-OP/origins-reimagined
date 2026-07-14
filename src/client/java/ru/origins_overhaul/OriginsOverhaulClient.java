package ru.origins_overhaul;

import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import ru.origins_overhaul.client.ClientOriginCatalog;
import ru.origins_overhaul.profiles.PresentationProfileManager;
import ru.origins_overhaul.client.ClientSelectionConfig;
import ru.origins_overhaul.client.preview.PlayerPreviewController;
import ru.origins_overhaul.client.visual.profile.VisualProfileManager;
import ru.origins_overhaul.client.visual.anchor.SkinAnchorManager;
import ru.origins_overhaul.client.visual.render.ParticleAuraManager;
import ru.origins_overhaul.client.VisualDebugCommands;

public final class OriginsOverhaulClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSelectionConfig.load(Minecraft.getInstance().gameDirectory.toPath());
        SkinAnchorManager.load(Minecraft.getInstance().gameDirectory.toPath());
        ParticleAuraManager.register();
        VisualDebugCommands.register();
        PresentationProfileManager.setReloadCallback(() -> {
            ClientOriginCatalog.rebuild();
            PlayerPreviewController.invalidateModels();
        });
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(VisualProfileManager.INSTANCE);
        OriginDataLoadedCallback.EVENT.register(fromServer -> {
            if (fromServer) ClientOriginCatalog.rebuild();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { ClientOriginCatalog.clear(); ParticleAuraManager.clear(); });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ClientOriginCatalog.clear());
        OriginsOverhaul.LOGGER.debug("Origins Overhaul client catalog initialized");
    }
}
