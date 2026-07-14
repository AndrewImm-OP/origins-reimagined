package ru.origins_overhaul.client;

public record OriginSelectionLayout(Rect header, Rect advantages, Rect preview, Rect disadvantages,
                                    Rect neutral, Rect navigation, Rect confirm, Rect listButton) {
    public static OriginSelectionLayout calculate(int width, int height) {
        int margin = Math.max(10, Math.min(34, width / 24));
        int headerHeight = Math.max(48, Math.min(72, height / 8));
        int bottom = Math.max(54, height / 8);
        int contentTop = margin + headerHeight;
        int neutralHeight = Math.max(42, Math.min(92, height / 7));
        int contentBottom = Math.max(contentTop + 50, height - bottom - neutralHeight);
        int gap = Math.max(8, width / 80);
        int usable = Math.max(180, width - margin * 2 - gap * 2);
        int side = Math.max(88, usable / 4);
        int center = Math.max(60, usable - side * 2);
        int x = margin;
        Rect left = new Rect(x, contentTop, side, Math.max(30, contentBottom - contentTop));
        x += side + gap;
        Rect middle = new Rect(x, contentTop, center, left.height());
        x += center + gap;
        Rect right = new Rect(x, contentTop, side, left.height());
        Rect h = new Rect(margin, margin, Math.max(1, width - margin * 2), headerHeight);
        Rect n = new Rect(margin, contentBottom + 8, Math.max(1, width - margin * 2), neutralHeight);
        Rect nav = new Rect(margin, height - bottom + 4, Math.max(1, width - margin * 2), 24);
        Rect confirm = new Rect(Math.max(margin, width / 2 - 55), height - 28, 110, 20);
        Rect list = new Rect(Math.max(margin, width - margin - 70), margin + 4, 70, 20);
        return new OriginSelectionLayout(h, left, middle, right, n, nav, confirm, list);
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double px, double py) { return px >= x && px < x + width && py >= y && py < y + height; }
    }
}
