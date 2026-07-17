package com.andrewimm.originsreimagined.networking;

import com.andrewimm.originsreimagined.OriginsReimagined;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpdateAdminFeaturePayload(Identifier origin, Identifier feature, boolean enabled) implements CustomPacketPayload {
    public static final Type<UpdateAdminFeaturePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OriginsReimagined.MOD_ID, "update_admin_feature"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateAdminFeaturePayload> CODEC = new StreamCodec<>() {
        @Override public UpdateAdminFeaturePayload decode(RegistryFriendlyByteBuf buf) { return new UpdateAdminFeaturePayload(buf.readIdentifier(), buf.readIdentifier(), buf.readBoolean()); }
        @Override public void encode(RegistryFriendlyByteBuf buf, UpdateAdminFeaturePayload value) { buf.writeIdentifier(value.origin); buf.writeIdentifier(value.feature); buf.writeBoolean(value.enabled); }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
