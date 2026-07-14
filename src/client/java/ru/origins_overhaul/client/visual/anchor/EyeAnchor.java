package ru.origins_overhaul.client.visual.anchor;

public record EyeAnchor(int x, int y, int width, int height, EyeLayer layer) {
    public EyeAnchor {
        x = clamp(x, 0, 7); y = clamp(y, 0, 7); width = clamp(width, 0, 8 - x); height = clamp(height, 0, 8 - y); layer = layer == null ? EyeLayer.BASE : layer;
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
