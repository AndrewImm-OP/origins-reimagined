package com.andrewimm.originsreimagined.client;

import com.andrewimm.originsreimagined.client.preview.PlayerPreviewCamera;
import com.andrewimm.originsreimagined.client.preview.PlayerPreviewInputHandler;

public final class PlayerPreviewMathSelfTest {
    public static void main(String[] args) {
        PlayerPreviewCamera camera = new PlayerPreviewCamera();
        camera.rotateBy(100.0f, 200.0f, 1.0f);
        camera.update(1.0f, false, 0.0f, false);
        assert camera.pitch() <= PlayerPreviewCamera.MAX_PITCH;
        assert camera.pitch() >= PlayerPreviewCamera.MIN_PITCH;
        camera.zoomBy(100.0f, 1.0f);
        camera.panBy(1000.0f, -1000.0f, 1.0f);
        camera.update(1.0f, false, 0.0f, false);
        assert camera.zoom() <= PlayerPreviewCamera.MAX_ZOOM;
        assert camera.offsetX() <= PlayerPreviewCamera.MAX_PAN;
        assert camera.offsetY() >= -PlayerPreviewCamera.MAX_PAN;
        camera.reset();
        assert !camera.userInteracted() && camera.autoRotate();

        PlayerPreviewInputHandler input = new PlayerPreviewInputHandler();
        input.press(0, false, 0, 0, camera);
        assert input.dragMode() == PlayerPreviewInputHandler.DragMode.ROTATE;
        input.release();
        input.press(1, false, 0, 0, camera);
        assert input.dragMode() == PlayerPreviewInputHandler.DragMode.PAN;
        input.release();
        input.press(0, true, 0, 0, camera);
        assert camera.autoRotate();
    }
}
