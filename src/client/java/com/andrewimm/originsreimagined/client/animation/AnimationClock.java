package com.andrewimm.originsreimagined.client.animation;

public final class AnimationClock {
    private static final float MAX_DELTA_SECONDS = 0.1f;
    private long lastNanos = System.nanoTime();
    private long elapsedMilliseconds;

    public float deltaSeconds() {
        long now = System.nanoTime();
        long nanos = Math.max(0L, now - lastNanos);
        lastNanos = now;
        float delta = Math.min(MAX_DELTA_SECONDS, nanos / 1_000_000_000.0f);
        elapsedMilliseconds += (long) (delta * 1000.0f);
        return delta;
    }

    public long elapsedMilliseconds() { return elapsedMilliseconds; }
    public void reset() { lastNanos = System.nanoTime(); elapsedMilliseconds = 0L; }
}
