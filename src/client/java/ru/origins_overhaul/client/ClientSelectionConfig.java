package ru.origins_overhaul.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ClientSelectionConfig {
    private static boolean cinematic = true;
    private static boolean neutral = true;
    private static boolean namespace;
    private static int threshold = 12;
    private static float opacity = 0.70f;

    private ClientSelectionConfig() {}

    public static void load(Path gameDirectory) {
        Properties p = new Properties();
        Path file = gameDirectory.resolve("config/origins_overhaul.properties");
        try {
            if (Files.exists(file)) try (var reader = Files.newBufferedReader(file)) { p.load(reader); }
        } catch (Exception ignored) { }
        cinematic = bool(p, "cinematic_selection_screen", true);
        neutral = bool(p, "show_neutral_features", true);
        namespace = bool(p, "show_origin_namespace", false);
        threshold = clamp(intValue(p, "origin_list_threshold", 12), 1, 1000);
        opacity = clamp(floatValue(p, "selection_background_opacity", 0.70f), 0.25f, 0.90f);
    }
    public static boolean cinematic() { return cinematic; }
    public static boolean showNeutral() { return neutral; }
    public static boolean showNamespace() { return namespace; }
    public static int threshold() { return threshold; }
    public static float opacity() { return opacity; }
    private static boolean bool(Properties p, String k, boolean d) { return Boolean.parseBoolean(p.getProperty(k, Boolean.toString(d))); }
    private static int intValue(Properties p, String k, int d) { try { return Integer.parseInt(p.getProperty(k, "" + d)); } catch (Exception e) { return d; } }
    private static float floatValue(Properties p, String k, float d) { try { return Float.parseFloat(p.getProperty(k, "" + d)); } catch (Exception e) { return d; } }
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
