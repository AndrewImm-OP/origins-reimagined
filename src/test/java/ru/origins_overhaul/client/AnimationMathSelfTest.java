package ru.origins_overhaul.client;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import ru.origins_overhaul.client.animation.AnimatedTextLine;
import ru.origins_overhaul.client.animation.AnimatedValue;
import ru.origins_overhaul.client.animation.AnimationState;
import ru.origins_overhaul.client.animation.Easing;
import ru.origins_overhaul.client.animation.OriginTransitionController;
import ru.origins_overhaul.client.animation.TextRevealController;

public final class AnimationMathSelfTest {
    public static void main(String[] args) {
        AnimatedValue value = new AnimatedValue(0.0f);
        value.start(0.0f, 1.0f, 1.0f, Easing.LINEAR);
        value.update(0.5f);
        assert Math.abs(value.value() - 0.5f) < 0.001f;
        value.update(2.0f);
        assert value.finished() && value.value() == 1.0f;

        TextRevealController reveal = new TextRevealController();
        reveal.update(0.5f);
        assert reveal.visibleCharacters(20, 10.0f) == 5;
        reveal.skip();
        assert reveal.visibleCharacters(20, 10.0f) == 20;

        AnimatedTextLine unicode = AnimatedTextLine.from(FormattedCharSequence.forward("А🙂B", Style.EMPTY));
        assert unicode.glyphCount() == 3;
        assert unicode.visible(2).accept((index, style, codePoint) -> codePoint != 'B');

        OriginTransitionController transition = new OriginTransitionController(4, 0);
        transition.durations(0.1f, 0.1f);
        transition.request(1, true);
        assert transition.state() == AnimationState.SWITCHING_OUT;
        transition.update(0.2f, true);
        assert transition.displayedIndex() == 1 && transition.state() == AnimationState.SWITCHING_IN;
        transition.request(3, true);
        assert transition.state() == AnimationState.SWITCHING_OUT && transition.targetIndex() == 3;
    }
}
