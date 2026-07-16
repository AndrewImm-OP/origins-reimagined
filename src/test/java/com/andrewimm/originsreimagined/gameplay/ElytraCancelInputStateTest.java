package com.andrewimm.originsreimagined.gameplay;

public final class ElytraCancelInputStateTest {
    public static void main(String[] args) {
        ElytraCancelInputState state = new ElytraCancelInputState();
        assert state.update(true, true) : "Space must cancel flight";
        assert !state.update(true, true) : "held Space must not retrigger";
        assert !state.update(true, false) : "released Space must not trigger";
        assert state.update(true, true) : "pressing Space again must trigger";
        assert !state.update(false, true) : "flight must be active";

        state.reset();
        assert state.update(true, true) : "reset must arm the next press";
    }
}
