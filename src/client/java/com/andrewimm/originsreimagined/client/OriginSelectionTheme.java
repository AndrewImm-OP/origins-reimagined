package com.andrewimm.originsreimagined.client;

import net.minecraft.resources.Identifier;
import com.andrewimm.originsreimagined.model.OriginPresentation;

/** Lightweight visual theme data; it never changes origin mechanics or networking. */
public record OriginSelectionTheme(int accent, int advantageAccent, int disadvantageAccent, int previewGlow) {
    public static OriginSelectionTheme forOrigin(OriginPresentation origin) {
        Identifier id = origin.originId();
        return switch (id.getPath()) {
            case "arachnid" -> new OriginSelectionTheme(0xFF705080, 0xFF78B887, 0xFFC77777, 0xFF604970);
            case "phantom" -> new OriginSelectionTheme(0xFF54D9C6, 0xFF78B887, 0xFFC77777, 0xFF326F78);
            case "enderian" -> new OriginSelectionTheme(0xFFB353FF, 0xFF78B887, 0xFFC77777, 0xFF593477);
            case "merling" -> new OriginSelectionTheme(0xFF4BBFBD, 0xFF78B887, 0xFFC77777, 0xFF275F69);
            case "blazeborn" -> new OriginSelectionTheme(0xFFFF8A43, 0xFF78B887, 0xFFC77777, 0xFF7A3E27);
            case "feline" -> new OriginSelectionTheme(0xFFD09A55, 0xFF78B887, 0xFFC77777, 0xFF71522F);
            case "avian" -> new OriginSelectionTheme(0xFFD8D08A, 0xFF78B887, 0xFFC77777, 0xFF6E6A45);
            case "elytrian" -> new OriginSelectionTheme(0xFF9B9BEA, 0xFF78B887, 0xFFC77777, 0xFF51517A);
            case "shulk" -> new OriginSelectionTheme(0xFFB47BD7, 0xFF78B887, 0xFFC77777, 0xFF633F78);
            default -> new OriginSelectionTheme(origin.customAccentColor().orElse(0xFF8AA0B8), 0xFF78B887, 0xFFC77777, 0xFF4B596B);
        };
    }
}
