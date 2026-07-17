package com.andrewimm.originsreimagined.networking;

import com.andrewimm.originsreimagined.OriginsReimagined;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenAdminSettingsPayload() implements CustomPacketPayload {
    public static final Type<OpenAdminSettingsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "open_admin_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAdminSettingsPayload> CODEC = StreamCodec.unit(new OpenAdminSettingsPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
