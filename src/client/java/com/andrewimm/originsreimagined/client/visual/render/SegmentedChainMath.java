package com.andrewimm.originsreimagined.client.visual.render;

public final class SegmentedChainMath {
    private SegmentedChainMath() {}
    public static int clampSegments(int value) { return Math.max(1, Math.min(16, value)); }
    public static float angle(float base, float time, float speed, float amplitude, int index, float phaseOffset, float weight, boolean reduceMotion) {
        if (reduceMotion) return base;
        return base + (float)Math.sin(time * speed - index * phaseOffset) * amplitude * weight;
    }
}
