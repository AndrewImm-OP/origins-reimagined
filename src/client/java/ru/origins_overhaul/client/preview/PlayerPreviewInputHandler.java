package ru.origins_overhaul.client.preview;

public final class PlayerPreviewInputHandler {
    public enum DragMode { NONE, ROTATE, PAN }
    private DragMode dragMode = DragMode.NONE;

    public boolean press(int button, boolean doubleClick, double x, double y, PlayerPreviewCamera camera) {
        if (doubleClick) { camera.reset(); dragMode = DragMode.NONE; return true; }
        if (button == 0) { dragMode = DragMode.ROTATE; return true; }
        if (button == 1) { dragMode = DragMode.PAN; return true; }
        return false;
    }

    public boolean drag(double deltaX, double deltaY, float sensitivity, PlayerPreviewCamera camera) {
        if (dragMode == DragMode.ROTATE) camera.rotateBy((float) deltaX, (float) deltaY, sensitivity);
        else if (dragMode == DragMode.PAN) camera.panBy((float) deltaX, (float) deltaY, sensitivity);
        else return false;
        return true;
    }

    public boolean release() { boolean active = dragMode != DragMode.NONE; dragMode = DragMode.NONE; return active; }
    public boolean scroll(double amount, float sensitivity, PlayerPreviewCamera camera) { camera.zoomBy((float) amount, sensitivity); return true; }
    public DragMode dragMode() { return dragMode; }
}
