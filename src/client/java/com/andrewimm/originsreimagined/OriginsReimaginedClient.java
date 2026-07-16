package com.andrewimm.originsreimagined;

import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import com.andrewimm.originsreimagined.client.ClientOriginCatalog;
import com.andrewimm.originsreimagined.profiles.PresentationProfileManager;
import com.andrewimm.originsreimagined.client.ClientSelectionConfig;
import com.andrewimm.originsreimagined.client.preview.PlayerPreviewController;
import com.andrewimm.originsreimagined.client.visual.profile.VisualProfileManager;
import com.andrewimm.originsreimagined.client.visual.anchor.SkinAnchorManager;
import com.andrewimm.originsreimagined.client.visual.render.ParticleAuraManager;
import com.andrewimm.originsreimagined.client.VisualDebugCommands;

public final class OriginsReimaginedClient implements ClientModInitializer {
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
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(PresentationProfileManager.INSTANCE);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(VisualProfileManager.INSTANCE);
        OriginDataLoadedCallback.EVENT.register(fromServer -> {
            if (fromServer) ClientOriginCatalog.rebuild();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { ClientOriginCatalog.clear(); ParticleAuraManager.clear(); });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ClientOriginCatalog.clear());
        OriginsReimagined.LOGGER.debug("Origins: Reimagined client catalog initialized");
    }
}
