package com.andrewimm.originsreimagined.client.animation;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import com.andrewimm.originsreimagined.model.PresentedPower;

import java.util.List;

public record AnimatedPowerContent(List<AnimatedTextLine> lines) {
    public static AnimatedPowerContent create(Font font, PresentedPower power, int width) {
        List<FormattedCharSequence> wrapped = font.split(power.description(), Math.max(1, width));
        return new AnimatedPowerContent(wrapped.stream().map(AnimatedTextLine::from).toList());
    }

    public int visibleCharacters(float elapsed, float speed, boolean enabled) {
        if (!enabled) return totalGlyphs();
        if (elapsed <= 0.0f) return 0;
        int glyphs = maxGlyphs();
        if (glyphs == 0) return 0;
        float duration = Math.max(0.12f, Math.min(0.85f, glyphs / Math.max(1.0f, speed)));
        return Math.max(0, Math.min(glyphs, (int) Math.floor((elapsed / duration) * glyphs)));
    }

    private int maxGlyphs() { return lines.stream().mapToInt(AnimatedTextLine::glyphCount).max().orElse(0); }
    private int totalGlyphs() { return lines.stream().mapToInt(AnimatedTextLine::glyphCount).max().orElse(0); }
}
