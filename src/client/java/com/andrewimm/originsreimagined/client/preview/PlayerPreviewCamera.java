package com.andrewimm.originsreimagined.client.preview;

public final class PlayerPreviewCamera {
    public static final float MIN_PITCH = -35.0f;
    public static final float MAX_PITCH = 35.0f;
    public static final float MIN_ZOOM = 0.70f;
    public static final float MAX_ZOOM = 1.55f;
    public static final float MAX_PAN = 0.35f;

    private float yaw = 25.0f;
    private float pitch = -5.0f;
    private float zoom = 1.12f;
    private float offsetX;
    private float offsetY = 0.08f;
    private float targetYaw = yaw;
    private float targetPitch = pitch;
    private float targetZoom = zoom;
    private float targetOffsetX;
    private float targetOffsetY = 0.08f;
    private boolean userInteracted;
    private boolean autoRotate = true;

    public void update(float deltaSeconds, boolean autoRotateEnabled, float autoRotateSpeed, boolean reduceMotion) {
        float delta = Math.min(0.1f, Math.max(0.0f, deltaSeconds));
        if (autoRotateEnabled && autoRotate && !userInteracted && !reduceMotion) targetYaw += autoRotateSpeed * delta;
        float factor = 1.0f - (float) Math.pow(0.001f, delta * 12.0f);
        yaw = damp(yaw, targetYaw, factor);
        pitch = damp(pitch, targetPitch, factor);
        zoom = damp(zoom, targetZoom, factor);
        offsetX = damp(offsetX, targetOffsetX, factor);
        offsetY = damp(offsetY, targetOffsetY, factor);
    }

    public void rotateBy(float deltaX, float deltaY, float sensitivity) {
        userInteracted = true;
        autoRotate = false;
        targetYaw += deltaX * sensitivity;
        targetPitch = clamp(targetPitch + deltaY * sensitivity * 0.55f, MIN_PITCH, MAX_PITCH);
    }

    public void zoomBy(float amount, float sensitivity) {
        userInteracted = true;
        autoRotate = false;
        targetZoom = clamp(targetZoom + amount * sensitivity * 0.08f, MIN_ZOOM, MAX_ZOOM);
    }

    public void panBy(float deltaX, float deltaY, float sensitivity) {
        userInteracted = true;
        autoRotate = false;
        targetOffsetX = clamp(targetOffsetX + deltaX * sensitivity * 0.003f, -MAX_PAN, MAX_PAN);
        targetOffsetY = clamp(targetOffsetY + deltaY * sensitivity * 0.003f, -MAX_PAN, MAX_PAN);
    }

    public void reset() {
        yaw = targetYaw = 25.0f;
        pitch = targetPitch = -5.0f;
        zoom = targetZoom = 1.12f;
        offsetX = targetOffsetX = 0.0f;
        offsetY = targetOffsetY = 0.08f;
        userInteracted = false;
        autoRotate = true;
    }

    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public float zoom() { return zoom; }
    public float offsetX() { return offsetX; }
    public float offsetY() { return offsetY; }
    public boolean userInteracted() { return userInteracted; }
    public boolean autoRotate() { return autoRotate; }
    private static float damp(float current, float target, float factor) { return current + (target - current) * factor; }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
}
