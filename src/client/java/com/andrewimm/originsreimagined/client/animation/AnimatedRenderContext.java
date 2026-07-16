package com.andrewimm.originsreimagined.client.animation;

public final class AnimatedRenderContext {
    private AnimatedRenderContext() {}

    public static int alpha(int argb, float opacity) {
        int alpha = Math.max(0, Math.min(255, Math.round(((argb >>> 24) & 0xFF) * Math.max(0.0f, Math.min(1.0f, opacity)))));
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }
}
