package com.andrewimm.originsreimagined.client.visual.context;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import io.github.apace100.origins.registry.ModComponents;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;

public final class WorldPlayerVisualContext implements PlayerVisualContext {
    private final Player player;
    private final PlayerAppearanceSnapshot appearance;
    private final float partialTick;
    private final boolean local;

    public WorldPlayerVisualContext(Player player, PlayerAppearanceSnapshot appearance, float partialTick) {
        this.player = player;
        this.appearance = appearance;
        this.partialTick = partialTick;
        this.local = Minecraft.getInstance().player == player;
    }

    @Override public java.util.UUID playerId() { return player.getUUID(); }
    @Override public Identifier originId() { return ModComponents.ORIGIN.get(player).getOrigins().values().stream().findFirst().map(origin -> origin.getIdentifier()).orElse(null); }
    @Override public boolean preview() { return false; }
    @Override public boolean localPlayer() { return local; }
    @Override public boolean powerActive(Identifier powerId) {
        if (powerId == null || !PowerTypeRegistry.contains(powerId)) return false;
        return PowerHolderComponent.KEY.get(player).hasPower(PowerTypeRegistry.get(powerId));
    }
    @Override public float partialTick() { return partialTick; }
    @Override public PlayerAppearanceSnapshot appearance() { return appearance; }
    @Override public boolean inWater() { return player.isInWater(); }
    @Override public boolean onFire() { return player.isOnFire(); }
    @Override public boolean sneaking() { return player.isCrouching(); }
    @Override public boolean swimming() { return player.isSwimming(); }
    @Override public boolean fallFlying() { return player.isFallFlying(); }
}
