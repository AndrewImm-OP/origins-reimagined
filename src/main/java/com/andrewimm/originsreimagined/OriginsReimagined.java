package com.andrewimm.originsreimagined;

import io.github.apace100.origins.Origins;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.andrewimm.originsreimagined.profiles.PresentationProfileManager;
import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.andrewimm.originsreimagined.networking.StopElytraFlightPayload;
import com.andrewimm.originsreimagined.gameplay.MerlingMechanics;
import com.andrewimm.originsreimagined.gameplay.PhantomMechanics;

public final class OriginsReimagined implements ModInitializer {
    public static final String MOD_ID = "origins_reimagined";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean DEBUG = Boolean.getBoolean("origins_reimagined.debug");

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(PresentationProfileManager.INSTANCE);
        MerlingMechanics.register();
        PhantomMechanics.register();
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
