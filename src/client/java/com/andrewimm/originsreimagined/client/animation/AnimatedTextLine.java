package com.andrewimm.originsreimagined.client.animation;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class AnimatedTextLine {
    private final List<Glyph> glyphs;

    private AnimatedTextLine(List<Glyph> glyphs) { this.glyphs = List.copyOf(glyphs); }

    public static AnimatedTextLine from(FormattedCharSequence line) {
        List<Glyph> glyphs = new ArrayList<>();
        line.accept((index, style, codePoint) -> { glyphs.add(new Glyph(codePoint, style)); return true; });
        return new AnimatedTextLine(glyphs);
    }

    public int glyphCount() { return glyphs.size(); }
    public FormattedCharSequence visible(int count) {
        int visible = Math.max(0, Math.min(count, glyphs.size()));
        if (visible == 0) return FormattedCharSequence.EMPTY;
        List<FormattedCharSequence> pieces = new ArrayList<>(visible);
        for (int index = 0; index < visible; index++) {
            Glyph glyph = glyphs.get(index);
            pieces.add(FormattedCharSequence.codepoint(glyph.codePoint(), glyph.style()));
        }
        return FormattedCharSequence.composite(pieces);
    }

    private record Glyph(int codePoint, Style style) { }
}
