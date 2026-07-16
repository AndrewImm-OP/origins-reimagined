package com.andrewimm.originsreimagined.client.preview;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Options;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Optional;
import java.util.UUID;

public record PlayerAppearanceSnapshot(
    UUID playerId,
    GameProfile gameProfile,
    PlayerSkin skin,
    Identifier skinTexture,
    PlayerModelType modelType,
    boolean showHat,
    boolean showJacket,
    boolean showLeftSleeve,
    boolean showRightSleeve,
    boolean showLeftPants,
    boolean showRightPants,
    Optional<Identifier> capeTexture,
    Optional<Identifier> elytraTexture
) {
    public static PlayerAppearanceSnapshot from(AbstractClientPlayer player, Options options) {
        PlayerSkin skin = player.getSkin();
        if (skin == null) skin = DefaultPlayerSkin.get(player.getGameProfile());
        return new PlayerAppearanceSnapshot(
            player.getUUID(), player.getGameProfile(), skin, skin.body().texturePath(), skin.model(),
            options.isModelPartEnabled(PlayerModelPart.HAT),
            options.isModelPartEnabled(PlayerModelPart.JACKET),
            options.isModelPartEnabled(PlayerModelPart.LEFT_SLEEVE),
            options.isModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE),
            options.isModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG),
            options.isModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG),
            Optional.ofNullable(skin.cape()).map(texture -> texture.texturePath()),
            Optional.ofNullable(skin.elytra()).map(texture -> texture.texturePath())
        );
    }

    public static PlayerAppearanceSnapshot fallback(GameProfile profile) {
        PlayerSkin skin = DefaultPlayerSkin.get(profile);
        return new PlayerAppearanceSnapshot(profile.id(), profile, skin, skin.body().texturePath(), skin.model(), true, true, true, true, true, true,
            Optional.ofNullable(skin.cape()).map(texture -> texture.texturePath()), Optional.ofNullable(skin.elytra()).map(texture -> texture.texturePath()));
    }
}
