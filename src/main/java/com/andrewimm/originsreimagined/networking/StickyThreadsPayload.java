package com.andrewimm.originsreimagined.networking;

import com.andrewimm.originsreimagined.OriginsReimagined;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client request; the server performs the raycast and all gameplay effects. */
public record StickyThreadsPayload() implements CustomPacketPayload {
    public static final Type<StickyThreadsPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "sticky_threads"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StickyThreadsPayload> CODEC =
        StreamCodec.unit(new StickyThreadsPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
