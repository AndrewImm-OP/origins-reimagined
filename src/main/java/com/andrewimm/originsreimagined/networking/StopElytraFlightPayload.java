package com.andrewimm.originsreimagined.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.andrewimm.originsreimagined.OriginsReimagined;

/** Server-validated request to stop the local player's elytra flight. */
public record StopElytraFlightPayload() implements CustomPacketPayload {
    public static final Type<StopElytraFlightPayload> TYPE =
        new Type<>(net.minecraft.resources.Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "stop_elytra_flight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StopElytraFlightPayload> CODEC =
        StreamCodec.unit(new StopElytraFlightPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
