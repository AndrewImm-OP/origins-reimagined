package com.andrewimm.originsreimagined.networking;

import com.andrewimm.originsreimagined.OriginsReimagined;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpdateAdminSettingPayload(String key, double value) implements CustomPacketPayload {
    public static final Type<UpdateAdminSettingPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "update_admin_setting"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateAdminSettingPayload> CODEC = new StreamCodec<>() {
        @Override public UpdateAdminSettingPayload decode(RegistryFriendlyByteBuf buf) { return new UpdateAdminSettingPayload(buf.readUtf(128), buf.readDouble()); }
        @Override public void encode(RegistryFriendlyByteBuf buf, UpdateAdminSettingPayload value) { buf.writeUtf(value.key, 128); buf.writeDouble(value.value); }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
