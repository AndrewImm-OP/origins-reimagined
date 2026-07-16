package com.andrewimm.originsreimagined.client.visual.context;

import net.minecraft.resources.Identifier;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;

import java.util.UUID;

public interface PlayerVisualContext {
    UUID playerId();
    Identifier originId();
    boolean preview();
    boolean localPlayer();
    boolean powerActive(Identifier powerId);
    float partialTick();
    PlayerAppearanceSnapshot appearance();
    default boolean inWater() { return false; }
    default boolean onFire() { return false; }
    default boolean sneaking() { return false; }
    default boolean swimming() { return false; }
    default boolean fallFlying() { return false; }
}
