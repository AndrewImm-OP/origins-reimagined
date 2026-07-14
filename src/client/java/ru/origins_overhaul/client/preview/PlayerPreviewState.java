package ru.origins_overhaul.client.preview;

public final class PlayerPreviewState {
    private PlayerAppearanceSnapshot appearance;
    private PreviewOriginContext originContext = PreviewOriginContext.empty();
    private boolean showOuterLayer = true;
    private boolean showEquipment;
    private boolean showCape;

    public PlayerAppearanceSnapshot appearance() { return appearance; }
    public PreviewOriginContext originContext() { return originContext; }
    public boolean showOuterLayer() { return showOuterLayer; }
    public boolean showEquipment() { return showEquipment; }
    public boolean showCape() { return showCape; }
    public void appearance(PlayerAppearanceSnapshot value) { appearance = value; }
    public void originContext(PreviewOriginContext value) { originContext = value == null ? PreviewOriginContext.empty() : value; }
    public void showOuterLayer(boolean value) { showOuterLayer = value; }
    public void showEquipment(boolean value) { showEquipment = value; }
    public void showCape(boolean value) { showCape = value; }
    public void clear() { appearance = null; originContext = PreviewOriginContext.empty(); }
}
