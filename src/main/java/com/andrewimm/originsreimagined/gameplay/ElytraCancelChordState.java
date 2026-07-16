package com.andrewimm.originsreimagined.gameplay;

/** Edge-triggered state for the Ctrl+Shift elytra-cancel chord. */
public final class ElytraCancelChordState {
    private boolean chordHeld;

    public boolean update(boolean fallFlying, boolean controlDown, boolean shiftDown) {
        boolean chord = fallFlying && controlDown && shiftDown;
        boolean triggered = chord && !chordHeld;
        chordHeld = chord;
        return triggered;
    }

    public void reset() {
        chordHeld = false;
    }
}
