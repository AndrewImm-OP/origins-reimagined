package ru.origins_overhaul.mixin;

import io.github.apace100.origins.networking.ModPacketsS2C;
import io.github.apace100.origins.networking.OpenOriginScreenPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.origins_overhaul.client.ClientOriginCatalog;
import ru.origins_overhaul.client.ClientSelectionConfig;
import ru.origins_overhaul.client.OriginSelectionSession;
import ru.origins_overhaul.client.screen.CinematicOriginSelectionScreen;

@Mixin(ModPacketsS2C.class)
public abstract class ModPacketsS2CMixin {
    @Inject(method = "openOriginScreen", at = @At("HEAD"), cancellable = true)
    private static void originsOverhaul$open(OpenOriginScreenPacket packet, ClientPlayNetworking.Context context, CallbackInfo callback) {
        if (!ClientSelectionConfig.cinematic()) return;
        callback.cancel();
        context.client().execute(() -> {
            ClientOriginCatalog.rebuild();
            if (context.player() == null) return;
            OriginSelectionSession session = OriginSelectionSession.fromServer(context.player(), packet.showDirtBackground());
            if (session.hasCurrentLayer()) context.client().setScreen(new CinematicOriginSelectionScreen(session));
        });
    }
}
