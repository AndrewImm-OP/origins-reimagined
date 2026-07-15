package ru.origins_overhaul.gameplay;

public final class ElytraCancelChordStateTest {
    public static void main(String[] args) {
        ElytraCancelChordState state = new ElytraCancelChordState();
        assert state.update(true, true, true);
        assert !state.update(true, true, true) : "held chord must not retrigger";
        assert !state.update(true, false, true) : "ordinary Shift must not trigger";
        assert state.update(true, true, true) : "pressing again must trigger";
        assert !state.update(false, true, true) : "flight must be active";

        state.reset();
        assert state.update(true, true, true) : "reset must arm the next press";
    }
}
