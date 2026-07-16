package com.andrewimm.originsreimagined.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.andrewimm.originsreimagined.gameplay.ElytraCancelChordState;
import com.andrewimm.originsreimagined.networking.StopElytraFlightPayload;

@Mixin(Minecraft.class)
public abstract class MinecraftElytraCancelMixin {
    private final ElytraCancelChordState originsOverhaul$chord = new ElytraCancelChordState();

    @Inject(method = "tick", at = @At("TAIL"))
    private void originsOverhaul$cancelElytraFlight(CallbackInfo callbackInfo) {
        Minecraft client = (Minecraft) (Object) this;
        boolean fallFlying = client.player != null && client.player.isFallFlying();
        boolean trigger = originsOverhaul$chord.update(
            fallFlying,
            client.hasControlDown(),
            client.options.keyShift.isDown()
        );
        if (!trigger || client.player == null) return;

        // Do not touch delta movement: leaving fall-flying naturally preserves velocity and direction.
        client.player.stopFallFlying();
        if (ClientPlayNetworking.canSend(StopElytraFlightPayload.TYPE)) {
            ClientPlayNetworking.send(new StopElytraFlightPayload());
        }
    }
}
