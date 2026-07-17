package com.andrewimm.originsreimagined.gameplay;

import com.andrewimm.originsreimagined.OriginsReimagined;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.LinkedHashMap;
import java.util.Map;

/** Server-side Merling settings. Values are read once at server startup. */
public final class MerlingConfig {
    public static final float DEFAULT_UNDERWATER_DAMAGE_MULTIPLIER = 1.20F;
    public static final float DEFAULT_FIRE_DAMAGE_MULTIPLIER = 1.50F;
    public static final int DEFAULT_NETHER_GRACE_TICKS = 200;
    public static final int DEFAULT_NETHER_DAMAGE_INTERVAL_TICKS = 40;
    public static final float DEFAULT_NETHER_DAMAGE = 1.0F;
    public static final int DEFAULT_ARACHNID_STICKY_COOLDOWN_TICKS = 240;
    public static final int DEFAULT_ARACHNID_WEB_DURATION_TICKS = 30;
    public static final int DEFAULT_ARACHNID_SLOW_DURATION_TICKS = 40;
    public static final float DEFAULT_ARACHNID_SPEED_MULTIPLIER = 1.10F;
    public static final float DEFAULT_FELINE_FOOD_SATURATION_MULTIPLIER = 1.20F;
    public static final float DEFAULT_FELINE_WET_SPEED_MULTIPLIER = 0.75F;
    public static final int DEFAULT_FELINE_WET_GRACE_TICKS = 80;

    private static volatile MerlingConfig current = defaults();

    public final float underwaterDamageMultiplier;
    public final float fireDamageMultiplier;
    public final int netherGraceTicks;
    public final int netherDamageIntervalTicks;
    public final float netherDamage;
    public final int arachnidStickyCooldownTicks;
    public final int arachnidWebDurationTicks;
    public final int arachnidSlowDurationTicks;
    public final float arachnidSpeedMultiplier;
    public final float felineFoodSaturationMultiplier;
    public final float felineWetSpeedMultiplier;
    public final int felineWetGraceTicks;

    private MerlingConfig(float underwaterDamageMultiplier, float fireDamageMultiplier,
                          int netherGraceTicks, int netherDamageIntervalTicks, float netherDamage,
                          int arachnidStickyCooldownTicks, int arachnidWebDurationTicks,
                          int arachnidSlowDurationTicks, float arachnidSpeedMultiplier,
                          float felineFoodSaturationMultiplier, float felineWetSpeedMultiplier,
                          int felineWetGraceTicks) {
        this.underwaterDamageMultiplier = underwaterDamageMultiplier;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.netherGraceTicks = netherGraceTicks;
        this.netherDamageIntervalTicks = netherDamageIntervalTicks;
        this.netherDamage = netherDamage;
        this.arachnidStickyCooldownTicks = arachnidStickyCooldownTicks;
        this.arachnidWebDurationTicks = arachnidWebDurationTicks;
        this.arachnidSlowDurationTicks = arachnidSlowDurationTicks;
        this.arachnidSpeedMultiplier = arachnidSpeedMultiplier;
        this.felineFoodSaturationMultiplier = felineFoodSaturationMultiplier;
        this.felineWetSpeedMultiplier = felineWetSpeedMultiplier;
        this.felineWetGraceTicks = felineWetGraceTicks;
    }

    public static MerlingConfig get() { return current; }

    public static Map<String, String> knownSettings() {
        MerlingConfig c = current;
        Map<String, String> values = new LinkedHashMap<>();
        values.put("merling.underwater_damage_multiplier", Float.toString(c.underwaterDamageMultiplier));
        values.put("merling.fire_damage_multiplier", Float.toString(c.fireDamageMultiplier));
        values.put("merling.nether_grace_ticks", Integer.toString(c.netherGraceTicks));
        values.put("merling.nether_damage_interval_ticks", Integer.toString(c.netherDamageIntervalTicks));
        values.put("merling.nether_damage", Float.toString(c.netherDamage));
        values.put("arachnid.sticky_cooldown_ticks", Integer.toString(c.arachnidStickyCooldownTicks));
        values.put("arachnid.web_duration_ticks", Integer.toString(c.arachnidWebDurationTicks));
        values.put("arachnid.slow_duration_ticks", Integer.toString(c.arachnidSlowDurationTicks));
        values.put("arachnid.speed_multiplier", Float.toString(c.arachnidSpeedMultiplier));
        values.put("feline.food_saturation_multiplier", Float.toString(c.felineFoodSaturationMultiplier));
        values.put("feline.wet_speed_multiplier", Float.toString(c.felineWetSpeedMultiplier));
        values.put("feline.wet_grace_ticks", Integer.toString(c.felineWetGraceTicks));
        return Map.copyOf(values);
    }

    public static boolean updateSetting(String key, double value) {
        if (!knownSettings().containsKey(key) || !Double.isFinite(value) || value < 0.0D) return false;
        Path path = Path.of("config", "origins_reimagined.properties");
        Properties properties = new Properties();
        try {
            if (Files.exists(path)) try (InputStream input = Files.newInputStream(path)) { properties.load(input); }
            properties.setProperty(key, key.endsWith("_ticks") ? Long.toString(Math.max(1L, Math.round(value))) : Double.toString(value));
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) { properties.store(output, "Origins: Reimagined settings"); }
            load();
            return true;
        } catch (IOException exception) {
            OriginsReimagined.LOGGER.warn("Could not update setting {}", key, exception);
            return false;
        }
    }

    public static void load() {
        Path path = Path.of("config", "origins_reimagined.properties");
        Properties properties = new Properties();
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                try (InputStream input = Files.newInputStream(path)) { properties.load(input); }
            }
            MerlingConfig loaded = new MerlingConfig(
                positiveFloat(properties, "merling.underwater_damage_multiplier", DEFAULT_UNDERWATER_DAMAGE_MULTIPLIER),
                positiveFloat(properties, "merling.fire_damage_multiplier", DEFAULT_FIRE_DAMAGE_MULTIPLIER),
                positiveInt(properties, "merling.nether_grace_ticks", DEFAULT_NETHER_GRACE_TICKS),
                positiveInt(properties, "merling.nether_damage_interval_ticks", DEFAULT_NETHER_DAMAGE_INTERVAL_TICKS),
                positiveFloat(properties, "merling.nether_damage", DEFAULT_NETHER_DAMAGE),
                positiveInt(properties, "arachnid.sticky_cooldown_ticks", DEFAULT_ARACHNID_STICKY_COOLDOWN_TICKS),
                positiveInt(properties, "arachnid.web_duration_ticks", DEFAULT_ARACHNID_WEB_DURATION_TICKS),
                positiveInt(properties, "arachnid.slow_duration_ticks", DEFAULT_ARACHNID_SLOW_DURATION_TICKS),
                positiveFloat(properties, "arachnid.speed_multiplier", DEFAULT_ARACHNID_SPEED_MULTIPLIER),
                positiveFloat(properties, "feline.food_saturation_multiplier", DEFAULT_FELINE_FOOD_SATURATION_MULTIPLIER),
                boundedMultiplier(properties, "feline.wet_speed_multiplier", DEFAULT_FELINE_WET_SPEED_MULTIPLIER),
                positiveInt(properties, "feline.wet_grace_ticks", DEFAULT_FELINE_WET_GRACE_TICKS)
            );
            properties.setProperty("merling.underwater_damage_multiplier", Float.toString(loaded.underwaterDamageMultiplier));
            properties.setProperty("merling.fire_damage_multiplier", Float.toString(loaded.fireDamageMultiplier));
            properties.setProperty("merling.nether_grace_ticks", Integer.toString(loaded.netherGraceTicks));
            properties.setProperty("merling.nether_damage_interval_ticks", Integer.toString(loaded.netherDamageIntervalTicks));
            properties.setProperty("merling.nether_damage", Float.toString(loaded.netherDamage));
            properties.setProperty("arachnid.sticky_cooldown_ticks", Integer.toString(loaded.arachnidStickyCooldownTicks));
            properties.setProperty("arachnid.web_duration_ticks", Integer.toString(loaded.arachnidWebDurationTicks));
            properties.setProperty("arachnid.slow_duration_ticks", Integer.toString(loaded.arachnidSlowDurationTicks));
            properties.setProperty("arachnid.speed_multiplier", Float.toString(loaded.arachnidSpeedMultiplier));
            properties.setProperty("feline.food_saturation_multiplier", Float.toString(loaded.felineFoodSaturationMultiplier));
            properties.setProperty("feline.wet_speed_multiplier", Float.toString(loaded.felineWetSpeedMultiplier));
            properties.setProperty("feline.wet_grace_ticks", Integer.toString(loaded.felineWetGraceTicks));
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "Origins: Reimagined Merling settings");
            }
            current = loaded;
        } catch (IOException exception) {
            OriginsReimagined.LOGGER.warn("Could not load Merling config; using safe defaults", exception);
            current = defaults();
        }
    }

    private static MerlingConfig defaults() {
        return new MerlingConfig(DEFAULT_UNDERWATER_DAMAGE_MULTIPLIER, DEFAULT_FIRE_DAMAGE_MULTIPLIER,
            DEFAULT_NETHER_GRACE_TICKS, DEFAULT_NETHER_DAMAGE_INTERVAL_TICKS, DEFAULT_NETHER_DAMAGE,
            DEFAULT_ARACHNID_STICKY_COOLDOWN_TICKS, DEFAULT_ARACHNID_WEB_DURATION_TICKS,
            DEFAULT_ARACHNID_SLOW_DURATION_TICKS, DEFAULT_ARACHNID_SPEED_MULTIPLIER,
            DEFAULT_FELINE_FOOD_SATURATION_MULTIPLIER, DEFAULT_FELINE_WET_SPEED_MULTIPLIER,
            DEFAULT_FELINE_WET_GRACE_TICKS);
    }

    private static float positiveFloat(Properties properties, String key, float fallback) {
        try {
            float value = Float.parseFloat(properties.getProperty(key, Float.toString(fallback)));
            return Float.isFinite(value) && value >= 0.0F ? value : fallback;
        } catch (NumberFormatException ignored) { return fallback; }
    }

    private static int positiveInt(Properties properties, String key, int fallback) {
        try {
            int value = Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)));
            return value >= 1 ? value : fallback;
        } catch (NumberFormatException ignored) { return fallback; }
    }

    private static float boundedMultiplier(Properties properties, String key, float fallback) {
        float value = positiveFloat(properties, key, fallback);
        return value > 0.0F && value <= 2.0F ? value : fallback;
    }
}
