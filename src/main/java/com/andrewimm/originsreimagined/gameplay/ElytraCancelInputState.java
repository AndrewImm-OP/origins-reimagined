package com.andrewimm.originsreimagined.gameplay;

/** Edge-triggered state for the Space elytra-cancel input. */
public final class ElytraCancelInputState {
    private boolean jumpHeld;

    public boolean update(boolean fallFlying, boolean jumpDown) {
        boolean pressed = fallFlying && jumpDown;
        boolean triggered = pressed && !jumpHeld;
        jumpHeld = pressed;
        return triggered;
    }

    public void reset() {
        jumpHeld = false;
    }
}
