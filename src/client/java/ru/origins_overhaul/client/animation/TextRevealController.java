package ru.origins_overhaul.client.animation;

public final class TextRevealController {
    private float elapsed;
    private boolean skipped;

    public void reset() { elapsed = 0.0f; skipped = false; }
    public void update(float deltaSeconds) { if (!skipped) elapsed += Math.max(0.0f, deltaSeconds); }
    public void skip() { skipped = true; }
    public boolean skipped() { return skipped; }
    public float elapsedSeconds() { return elapsed; }

    public int visibleCharacters(int glyphCount, float speed) {
        if (skipped || glyphCount <= 0) return glyphCount;
        if (speed <= 0.0f) return 0;
        return Math.max(0, Math.min(glyphCount, (int) Math.floor(elapsed * speed)));
    }

    public float abilityElapsed(int abilityIndex, float staggerSeconds) {
        return Math.max(0.0f, elapsed - Math.max(0, abilityIndex) * Math.max(0.0f, staggerSeconds));
    }
}
