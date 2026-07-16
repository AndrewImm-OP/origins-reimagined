package ru.origins_overhaul;

import io.github.apace100.origins.Origins;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.origins_overhaul.profiles.PresentationProfileManager;
import ru.origins_overhaul.compat.originslegacy.OriginsLegacyAdapter;
import ru.origins_overhaul.networking.StopElytraFlightPayload;

public final class OriginsOverhaul implements ModInitializer {
    public static final String MOD_ID = "origins_overhaul";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean DEBUG = Boolean.getBoolean("origins_overhaul.debug");

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(PresentationProfileManager.INSTANCE);
        PayloadTypeRegistry.serverboundPlay().register(StopElytraFlightPayload.TYPE, StopElytraFlightPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(StopElytraFlightPayload.TYPE, (payload, context) ->
            context.server().execute(() -> {
                var player = context.player();
                Identifier elytrian = Identifier.fromNamespaceAndPath("origins", "elytrian");
                if (player.isFallFlying()
                    && OriginsLegacyAdapter.hasOrigin(player, elytrian)) {
                    // stopFallFlying only changes the flight flag; the existing delta movement is untouched.
                    player.stopFallFlying();
                }
            })
        );
        LOGGER.info("Origins: Reimagined initialized for Minecraft 26.1.2 (Origins Legacy {})", Origins.VERSION);
    }
}
