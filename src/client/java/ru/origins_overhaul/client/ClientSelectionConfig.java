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
    private static boolean textAnimation = true;
    private static float textSpeed = 42.0f;
    private static boolean transitionAnimation = true;
    private static float transitionOut = 0.15f;
    private static float transitionIn = 0.21f;
    private static boolean iconRotation = true;
    private static float iconRotationSpeed = 18.0f;
    private static boolean iconBob = true;
    private static float abilityStagger = 0.075f;
    private static boolean reduceMotion;
    private static boolean previewEnabled = true;
    private static boolean previewAutoRotate = true;
    private static float previewAutoRotateSpeed = 12.0f;
    private static float previewMouseSensitivity = 1.0f;
    private static float previewZoomSensitivity = 1.0f;
    private static boolean previewShowOuterLayer = true;
    private static boolean previewShowCape;
    private static boolean previewShowEquipment;
    private static boolean previewIdleAnimation = true;

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
        textAnimation = bool(p, "text_animation_enabled", true);
        textSpeed = clamp(floatValue(p, "text_animation_speed", 42.0f), 1.0f, 300.0f);
        transitionAnimation = bool(p, "transition_animation_enabled", true);
        transitionOut = clamp(floatValue(p, "transition_out_duration_ms", 150.0f), 0.0f, 2000.0f) / 1000.0f;
        transitionIn = clamp(floatValue(p, "transition_in_duration_ms", 210.0f), 0.0f, 2000.0f) / 1000.0f;
        iconRotation = bool(p, "icon_rotation_enabled", true);
        iconRotationSpeed = clamp(floatValue(p, "icon_rotation_speed", 18.0f), -360.0f, 360.0f);
        iconBob = bool(p, "icon_bob_enabled", true);
        abilityStagger = clamp(floatValue(p, "ability_stagger_ms", 75.0f), 0.0f, 1000.0f) / 1000.0f;
        reduceMotion = bool(p, "reduce_motion", false);
        previewEnabled = bool(p, "player_preview_enabled", true);
        previewAutoRotate = bool(p, "preview_auto_rotate", true);
        previewAutoRotateSpeed = clamp(floatValue(p, "preview_auto_rotate_speed", 12.0f), -180.0f, 180.0f);
        previewMouseSensitivity = clamp(floatValue(p, "preview_mouse_sensitivity", 1.0f), 0.1f, 5.0f);
        previewZoomSensitivity = clamp(floatValue(p, "preview_zoom_sensitivity", 1.0f), 0.1f, 5.0f);
        previewShowOuterLayer = bool(p, "preview_show_outer_layer", true);
        previewShowCape = bool(p, "preview_show_cape", false);
        previewShowEquipment = bool(p, "preview_show_equipment", false);
        previewIdleAnimation = bool(p, "preview_idle_animation", true);
    }
    public static boolean cinematic() { return cinematic; }
    public static boolean showNeutral() { return neutral; }
    public static boolean showNamespace() { return namespace; }
    public static int threshold() { return threshold; }
    public static float opacity() { return opacity; }
    public static boolean textAnimation() { return textAnimation && !reduceMotion; }
    public static float textSpeed() { return textSpeed; }
    public static boolean transitionAnimation() { return transitionAnimation && !reduceMotion; }
    public static float transitionOut() { return reduceMotion ? 0.0f : transitionOut; }
    public static float transitionIn() { return reduceMotion ? 0.08f : transitionIn; }
    public static boolean iconRotation() { return iconRotation && !reduceMotion; }
    public static float iconRotationSpeed() { return reduceMotion ? 0.0f : iconRotationSpeed; }
    public static boolean iconBob() { return iconBob && !reduceMotion; }
    public static float abilityStagger() { return reduceMotion ? 0.0f : abilityStagger; }
    public static boolean reduceMotion() { return reduceMotion; }
    public static boolean previewEnabled() { return previewEnabled; }
    public static boolean previewAutoRotate() { return previewAutoRotate; }
    public static float previewAutoRotateSpeed() { return previewAutoRotateSpeed; }
    public static float previewMouseSensitivity() { return previewMouseSensitivity; }
    public static float previewZoomSensitivity() { return previewZoomSensitivity; }
    public static boolean previewShowOuterLayer() { return previewShowOuterLayer; }
    public static boolean previewShowCape() { return previewShowCape; }
    public static boolean previewShowEquipment() { return previewShowEquipment; }
    public static boolean previewIdleAnimation() { return previewIdleAnimation && !reduceMotion; }
    private static boolean bool(Properties p, String k, boolean d) { return Boolean.parseBoolean(p.getProperty(k, Boolean.toString(d))); }
    private static int intValue(Properties p, String k, int d) { try { return Integer.parseInt(p.getProperty(k, "" + d)); } catch (Exception e) { return d; } }
    private static float floatValue(Properties p, String k, float d) { try { return Float.parseFloat(p.getProperty(k, "" + d)); } catch (Exception e) { return d; } }
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
