package com.andrewimm.originsreimagined.client;

public record OriginSelectionLayout(Rect header, Rect advantages, Rect preview, Rect disadvantages,
                                    Rect neutral, Rect navigation, Rect random, Rect confirm, Rect listButton) {
    public static OriginSelectionLayout calculate(int width, int height) {
        return calculate(width, height, true);
    }

    public static OriginSelectionLayout calculate(int width, int height, boolean hasNeutralFeatures) {
        int margin = Math.max(16, Math.min(40, width / 32));
        int headerHeight = Math.max(48, Math.min(56, height / 9));
        // The navigation and action buttons live inside the central panel;
        // reserve only a small safety margin below that panel.
        int bottom = Math.max(48, Math.min(64, height / 10));
        int contentTop = margin + headerHeight + 8;
        int availableBelowHeader = Math.max(84, height - contentTop - bottom);
        int neutralHeight = hasNeutralFeatures ? Math.max(42, Math.min(92, availableBelowHeader / 4)) : 0;
        int neutralY = hasNeutralFeatures ? height - bottom - neutralHeight - 6 : height - bottom;
        int contentBottom = Math.max(contentTop + 50, neutralY - (hasNeutralFeatures ? 8 : 0));
        int gap = Math.max(10, width / 80);
        int usable = Math.max(180, width - margin * 2 - gap * 2);
        int side = Math.max(120, usable * 31 / 100);
        int center = Math.max(80, usable - side * 2);
        int x = margin;
        Rect left = new Rect(x, contentTop, side, Math.max(30, contentBottom - contentTop));
        x += side + gap;
        Rect middle = new Rect(x, margin, center, Math.max(80, contentBottom - margin));
        x += center + gap;
        Rect right = new Rect(x, contentTop, side, left.height());
        Rect h = new Rect(middle.x(), middle.y(), middle.width(), headerHeight);
        Rect n = hasNeutralFeatures ? new Rect(margin, neutralY, Math.max(1, width - margin * 2), neutralHeight) : new Rect(0, 0, 0, 0);
        int arrowTop = middle.y() + headerHeight + Math.max(0, (middle.height() - headerHeight) / 2) - 14;
        Rect nav = new Rect(middle.x() + 10, arrowTop, Math.max(1, middle.width() - 20), 28);
        int actionWidth = 96 + 10 + 110;
        int actionX = middle.x() + Math.max(0, (middle.width() - actionWidth) / 2);
        Rect random = new Rect(actionX, middle.y() + middle.height() - 27, 96, 20);
        Rect confirm = new Rect(actionX + 106, middle.y() + middle.height() - 27, 110, 20);
        Rect list = new Rect(Math.max(margin, width - margin - 70), margin + 4, 70, 20);
        return new OriginSelectionLayout(h, left, middle, right, n, nav, random, confirm, list);
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double px, double py) { return px >= x && px < x + width && py >= y && py < y + height; }
    }
}
