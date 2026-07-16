package com.andrewimm.originsreimagined.profiles;

public final class OriginDifficultyColorResolver {
    private OriginDifficultyColorResolver() {}

    public static int resolve(int impact, Integer customColor) {
        if (customColor != null && customColor != 0) return customColor;
        return switch (impact) {
            case 0 -> 0xFFD0D0D0;
            case 1 -> 0xFF55CC66;
            case 2 -> 0xFFFFC84A;
            default -> 0xFFE55353;
        };
    }
}
