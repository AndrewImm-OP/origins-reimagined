package com.andrewimm.originsreimagined.client.animation;

public enum Easing {
    LINEAR {
        @Override public float apply(float progress) { return progress; }
    },
    EASE_OUT_CUBIC {
        @Override public float apply(float progress) {
            float inverse = 1.0f - progress;
            return 1.0f - inverse * inverse * inverse;
        }
    },
    EASE_IN_OUT_CUBIC {
        @Override public float apply(float progress) {
            return progress < 0.5f ? 4.0f * progress * progress * progress : 1.0f - (float) Math.pow(-2.0f * progress + 2.0f, 3.0f) / 2.0f;
        }
    };

    public abstract float apply(float progress);
}
