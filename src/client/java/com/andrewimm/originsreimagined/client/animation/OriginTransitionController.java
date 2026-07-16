package com.andrewimm.originsreimagined.client.animation;

public final class OriginTransitionController {
    private final int size;
    private int displayedIndex;
    private int targetIndex;
    private int direction;
    private AnimationState state = AnimationState.IDLE;
    private final AnimatedValue progress = new AnimatedValue(1.0f);
    private float outDuration = 0.15f;
    private float inDuration = 0.21f;

    public OriginTransitionController(int size, int initialIndex) {
        this.size = Math.max(0, size);
        this.displayedIndex = this.size == 0 ? 0 : Math.floorMod(initialIndex, this.size);
        this.targetIndex = displayedIndex;
    }

    public void durations(float outSeconds, float inSeconds) { outDuration = Math.max(0.0f, outSeconds); inDuration = Math.max(0.0f, inSeconds); }

    public boolean request(int index, boolean enabled) {
        if (size <= 1) return false;
        int next = Math.floorMod(index, size);
        if (!enabled) { displayedIndex = next; targetIndex = next; state = AnimationState.IDLE; progress.start(1, 1, 0, Easing.LINEAR); return true; }
        if (next == targetIndex && state != AnimationState.SWITCHING_OUT) return false;
        direction = Integer.compare(next, targetIndex);
        targetIndex = next;
        if (state == AnimationState.SWITCHING_IN) {
            state = AnimationState.SWITCHING_OUT;
            progress.start(1.0f, 0.0f, outDuration, Easing.EASE_OUT_CUBIC);
        } else if (state != AnimationState.SWITCHING_OUT) {
            state = AnimationState.SWITCHING_OUT;
            progress.start(1.0f, 0.0f, outDuration, Easing.EASE_OUT_CUBIC);
        }
        return true;
    }

    public boolean update(float deltaSeconds, boolean enabled) {
        if (!enabled) return false;
        progress.update(deltaSeconds);
        if (state == AnimationState.SWITCHING_OUT && progress.finished()) {
            displayedIndex = targetIndex;
            state = AnimationState.SWITCHING_IN;
            progress.start(0.0f, 1.0f, inDuration, Easing.EASE_OUT_CUBIC);
            return true;
        }
        if (state == AnimationState.SWITCHING_IN && progress.finished()) state = AnimationState.IDLE;
        return false;
    }

    public int displayedIndex() { return displayedIndex; }
    public int targetIndex() { return targetIndex; }
    public int direction() { return direction; }
    public AnimationState state() { return state; }
    public float progress() { return progress.value(); }
}
