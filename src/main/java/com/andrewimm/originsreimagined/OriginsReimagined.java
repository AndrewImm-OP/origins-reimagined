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
import com.andrewimm.originsreimagined.networking.StickyThreadsPayload;
import com.andrewimm.originsreimagined.networking.OpenAdminSettingsPayload;
import com.andrewimm.originsreimagined.networking.UpdateAdminSettingPayload;
import com.andrewimm.originsreimagined.networking.UpdateAdminFeaturePayload;
import com.andrewimm.originsreimagined.gameplay.MerlingMechanics;
import com.andrewimm.originsreimagined.gameplay.PhantomMechanics;
import com.andrewimm.originsreimagined.gameplay.AdminFeatureControl;
import com.andrewimm.originsreimagined.gameplay.ArachnidMechanics;
import com.andrewimm.originsreimagined.gameplay.FelineMechanics;

public final class OriginsReimagined implements ModInitializer {
    public static final String MOD_ID = "origins_reimagined";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean DEBUG = Boolean.getBoolean("origins_reimagined.debug");
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(PresentationProfileManager.INSTANCE);
        AdminFeatureControl.register();
        MerlingMechanics.register();
        PhantomMechanics.register();
        ArachnidMechanics.register();
        FelineMechanics.register();
        PayloadTypeRegistry.serverboundPlay().register(StopElytraFlightPayload.TYPE, StopElytraFlightPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(StickyThreadsPayload.TYPE, StickyThreadsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenAdminSettingsPayload.TYPE, OpenAdminSettingsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdateAdminSettingPayload.TYPE, UpdateAdminSettingPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdateAdminFeaturePayload.TYPE, UpdateAdminFeaturePayload.CODEC);
        AdminFeatureControl.registerSettingsNetworking();
        ServerPlayNetworking.registerGlobalReceiver(StopElytraFlightPayload.TYPE, (payload, context) ->
            context.server().execute(() -> {
                var player = context.player();
                Identifier elytrian = Identifier.fromNamespaceAndPath("origins", "elytrian");
                if (player.isFallFlying()
                    && OriginsLegacyAdapter.hasOrigin(player, elytrian)
                    && AdminFeatureControl.enabled(player, elytrian,
                        Identifier.fromNamespaceAndPath(MOD_ID, "elytrian_flight_cancel"))) {
                    // stopFallFlying only changes the flight flag; the existing delta movement is untouched.
                    player.stopFallFlying();
                }
            })
        );
        LOGGER.info("Origins: Reimagined initialized for Minecraft 26.1.2 (Origins Legacy {})", Origins.VERSION);
    }
}
