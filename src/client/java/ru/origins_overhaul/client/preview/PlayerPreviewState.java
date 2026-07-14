package ru.origins_overhaul.client.preview;

import net.minecraft.resources.Identifier;
import java.util.Set;

public final class PlayerPreviewState {
    private PlayerAppearanceSnapshot appearance;
    private PreviewOriginContext originContext = PreviewOriginContext.empty();
    private boolean showOuterLayer = true;
    private boolean showEquipment;
    private boolean showCape;
    private Set<Identifier> simulatedPowers = Set.of();
    private boolean inWater;
    private boolean swimming;
    private boolean sneaking;
    private boolean fallFlying;
    private boolean onFire;
    private String modelOverride = "AUTO";

    public PlayerAppearanceSnapshot appearance() { return appearance; }
    public PreviewOriginContext originContext() { return originContext; }
    public boolean showOuterLayer() { return showOuterLayer; }
    public boolean showEquipment() { return showEquipment; }
    public boolean showCape() { return showCape; }
    public Set<Identifier> simulatedPowers() { return simulatedPowers; }
    public boolean inWater() { return inWater; }
    public boolean swimming() { return swimming; }
    public boolean sneaking() { return sneaking; }
    public boolean fallFlying() { return fallFlying; }
    public boolean onFire() { return onFire; }
    public String modelOverride() { return modelOverride; }
    public void appearance(PlayerAppearanceSnapshot value) { appearance = value; }
    public void originContext(PreviewOriginContext value) { originContext = value == null ? PreviewOriginContext.empty() : value; }
    public void showOuterLayer(boolean value) { showOuterLayer = value; }
    public void showEquipment(boolean value) { showEquipment = value; }
    public void showCape(boolean value) { showCape = value; }
    public void simulatedPowers(Set<Identifier> value) { simulatedPowers = Set.copyOf(value == null ? Set.of() : value); }
    public void environment(boolean inWater, boolean swimming, boolean sneaking, boolean fallFlying, boolean onFire) { this.inWater = inWater; this.swimming = swimming; this.sneaking = sneaking; this.fallFlying = fallFlying; this.onFire = onFire; }
    public void modelOverride(String value) { modelOverride = value == null ? "AUTO" : value.toUpperCase(java.util.Locale.ROOT); }
    public void clear() { appearance = null; originContext = PreviewOriginContext.empty(); simulatedPowers = Set.of(); environment(false, false, false, false, false); modelOverride = "AUTO"; }
}
