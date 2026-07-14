package ru.origins_overhaul.client;

public record OriginSelectionLayout(Rect header, Rect advantages, Rect preview, Rect disadvantages,
                                    Rect neutral, Rect navigation, Rect confirm, Rect listButton) {
    public static OriginSelectionLayout calculate(int width, int height) {
        int margin = Math.max(16, Math.min(40, width / 32));
        int headerHeight = Math.max(56, Math.min(78, height / 7));
        int bottom = Math.max(78, Math.min(110, height / 7));
        int contentTop = margin + headerHeight + 8;
        int availableBelowHeader = Math.max(84, height - contentTop - bottom);
        int neutralHeight = Math.max(42, Math.min(92, availableBelowHeader / 4));
        int neutralY = height - bottom - neutralHeight - 6;
        int contentBottom = Math.max(contentTop + 50, neutralY - 8);
        int gap = Math.max(10, width / 80);
        int usable = Math.max(180, width - margin * 2 - gap * 2);
        int side = Math.max(120, usable * 27 / 100);
        int center = Math.max(80, usable - side * 2);
        int x = margin;
        Rect left = new Rect(x, contentTop, side, Math.max(30, contentBottom - contentTop));
        x += side + gap;
        Rect middle = new Rect(x, contentTop, center, left.height());
        x += center + gap;
        Rect right = new Rect(x, contentTop, side, left.height());
        Rect h = new Rect(margin, margin, Math.max(1, width - margin * 2), headerHeight);
        Rect n = new Rect(margin, neutralY, Math.max(1, width - margin * 2), neutralHeight);
        Rect nav = new Rect(margin, height - bottom + 8, Math.max(1, width - margin * 2), 28);
        Rect confirm = new Rect(Math.max(margin, width / 2 - 55), height - 28, 110, 20);
        Rect list = new Rect(Math.max(margin, width - margin - 70), margin + 4, 70, 20);
        return new OriginSelectionLayout(h, left, middle, right, n, nav, confirm, list);
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double px, double py) { return px >= x && px < x + width && py >= y && py < y + height; }
    }
}
