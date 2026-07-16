package com.andrewimm.originsreimagined.client.animation;

public final class AnimatedValue {
    private float from;
    private float to;
    private float duration;
    private float elapsed;
    private Easing easing = Easing.LINEAR;
    private float value;
    private boolean finished = true;

    public AnimatedValue(float initial) { value = initial; from = initial; to = initial; }

    public void start(float from, float to, float duration, Easing easing) {
        this.from = from;
        this.to = to;
        this.duration = Math.max(0.0f, duration);
        this.elapsed = 0.0f;
        this.easing = easing == null ? Easing.LINEAR : easing;
        this.value = from;
        this.finished = this.duration == 0.0f;
        if (finished) value = to;
    }

    public void update(float deltaSeconds) {
        if (finished) return;
        elapsed = Math.min(duration, elapsed + Math.max(0.0f, deltaSeconds));
        float progress = duration <= 0.0f ? 1.0f : elapsed / duration;
        value = from + (to - from) * easing.apply(Math.max(0.0f, Math.min(1.0f, progress)));
        if (elapsed >= duration) { value = to; finished = true; }
    }

    public void finish() { elapsed = duration; value = to; finished = true; }
    public float value() { return value; }
    public float progress() { return duration <= 0.0f ? 1.0f : Math.max(0.0f, Math.min(1.0f, elapsed / duration)); }
    public boolean finished() { return finished; }
}
