package com.andrewimm.originsreimagined.gameplay;

import com.andrewimm.originsreimagined.OriginsReimagined;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Server-side Merling settings. Values are read once at server startup. */
public final class MerlingConfig {
    public static final float DEFAULT_UNDERWATER_DAMAGE_MULTIPLIER = 1.20F;
    public static final float DEFAULT_FIRE_DAMAGE_MULTIPLIER = 1.50F;
    public static final int DEFAULT_NETHER_GRACE_TICKS = 200;
    public static final int DEFAULT_NETHER_DAMAGE_INTERVAL_TICKS = 40;
    public static final float DEFAULT_NETHER_DAMAGE = 1.0F;

    private static volatile MerlingConfig current = defaults();

    public final float underwaterDamageMultiplier;
    public final float fireDamageMultiplier;
    public final int netherGraceTicks;
    public final int netherDamageIntervalTicks;
    public final float netherDamage;

    private MerlingConfig(float underwaterDamageMultiplier, float fireDamageMultiplier,
                          int netherGraceTicks, int netherDamageIntervalTicks, float netherDamage) {
        this.underwaterDamageMultiplier = underwaterDamageMultiplier;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.netherGraceTicks = netherGraceTicks;
        this.netherDamageIntervalTicks = netherDamageIntervalTicks;
        this.netherDamage = netherDamage;
    }

    public static MerlingConfig get() { return current; }

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
                positiveFloat(properties, "merling.nether_damage", DEFAULT_NETHER_DAMAGE)
            );
            properties.setProperty("merling.underwater_damage_multiplier", Float.toString(loaded.underwaterDamageMultiplier));
            properties.setProperty("merling.fire_damage_multiplier", Float.toString(loaded.fireDamageMultiplier));
            properties.setProperty("merling.nether_grace_ticks", Integer.toString(loaded.netherGraceTicks));
            properties.setProperty("merling.nether_damage_interval_ticks", Integer.toString(loaded.netherDamageIntervalTicks));
            properties.setProperty("merling.nether_damage", Float.toString(loaded.netherDamage));
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
            DEFAULT_NETHER_GRACE_TICKS, DEFAULT_NETHER_DAMAGE_INTERVAL_TICKS, DEFAULT_NETHER_DAMAGE);
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
}
