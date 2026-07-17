package com.andrewimm.originsreimagined.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.andrewimm.originsreimagined.gameplay.ElytraCancelInputState;
import com.andrewimm.originsreimagined.networking.StopElytraFlightPayload;

@Mixin(Minecraft.class)
public abstract class MinecraftElytraCancelMixin {
    private final ElytraCancelInputState originsReimagined$input = new ElytraCancelInputState();

    @Inject(method = "tick", at = @At("TAIL"))
    private void originsReimagined$cancelElytraFlight(CallbackInfo callbackInfo) {
        Minecraft client = (Minecraft) (Object) this;
        boolean fallFlying = client.player != null && client.player.isFallFlying();
        boolean trigger = originsReimagined$input.update(fallFlying, client.options.keyJump.isDown());
        if (!trigger || client.player == null) return;

        // The server decides whether this feature is enabled and stops flight.
        // This avoids a client/server mismatch when an administrator disabled it.
        if (ClientPlayNetworking.canSend(StopElytraFlightPayload.TYPE)) {
            ClientPlayNetworking.send(new StopElytraFlightPayload());
        }
    }
}
