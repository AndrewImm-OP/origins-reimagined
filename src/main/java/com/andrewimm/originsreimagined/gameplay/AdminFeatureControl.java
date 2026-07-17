package com.andrewimm.originsreimagined.gameplay;

import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.compat.originslegacy.OriginsLegacyAdapter;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.andrewimm.originsreimagined.networking.OpenAdminSettingsPayload;
import com.andrewimm.originsreimagined.networking.UpdateAdminSettingPayload;
import com.andrewimm.originsreimagined.networking.UpdateAdminFeaturePayload;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Server-owned feature gates. Missing entries are enabled by default. */
public final class AdminFeatureControl {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("origins_reimagined_features.properties");
    private static final Map<String, Boolean> OVERRIDES = new HashMap<>();

    private AdminFeatureControl() {}

    public static void register() {
        load();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var set = Commands.literal("set")
                .then(Commands.argument("origin", StringArgumentType.word())
                    .then(Commands.argument("feature", StringArgumentType.word())
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                            .executes(context -> {
                                Identifier origin = Identifier.parse(StringArgumentType.getString(context, "origin"));
                                Identifier feature = Identifier.parse(StringArgumentType.getString(context, "feature"));
                                boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                set(origin, feature, enabled);
                                context.getSource().sendSuccess(() -> Component.literal(
                                    "Origins: Reimagined: " + origin + " / " + feature + " = " + enabled), true);
                                return 1;
                            }))));
            var reset = Commands.literal("reset")
                .then(Commands.argument("origin", StringArgumentType.word())
                    .executes(context -> {
                        Identifier origin = Identifier.parse(StringArgumentType.getString(context, "origin"));
                        resetOrigin(origin);
                        context.getSource().sendSuccess(() -> Component.literal(
                            "Origins: Reimagined: reset feature overrides for " + origin), true);
                        return 1;
                    })
                    .then(Commands.argument("feature", StringArgumentType.word())
                        .executes(context -> {
                            Identifier origin = Identifier.parse(StringArgumentType.getString(context, "origin"));
                            Identifier feature = Identifier.parse(StringArgumentType.getString(context, "feature"));
                            set(origin, feature, true);
                            context.getSource().sendSuccess(() -> Component.literal(
                                "Origins: Reimagined: reset " + origin + " / " + feature), true);
                            return 1;
                        })));
            var list = Commands.literal("list")
                .executes(context -> {
                    List<String> entries = OVERRIDES.entrySet().stream()
                        .filter(entry -> !entry.getValue())
                        .map(Map.Entry::getKey)
                        .sorted()
                        .toList();
                    context.getSource().sendSuccess(() -> Component.literal(entries.isEmpty()
                        ? "Origins: Reimagined: no disabled features"
                        : "Origins: Reimagined disabled: " + String.join(", ", entries)), false);
                    return entries.size();
                });
            var ui = Commands.literal("ui").executes(context -> {
                if (!context.getSource().isPlayer()) return 0;
                ServerPlayer player = context.getSource().getPlayer();
                if (ServerPlayNetworking.canSend(player, OpenAdminSettingsPayload.TYPE)) {
                    ServerPlayNetworking.send(player, new OpenAdminSettingsPayload());
                    return 1;
                }
                return 0;
            });
            var feature = Commands.literal("feature").then(set).then(reset).then(list);
            var admin = Commands.literal("admin").then(feature);
            admin.then(ui);
            dispatcher.register(Commands.literal("originsreimagined")
                .requires(source -> !source.isPlayer()
                    || source.getServer().getPlayerList().isOp(new NameAndId(source.getPlayer().getGameProfile())))
                .then(admin));
        });
        OriginsReimagined.LOGGER.info("Admin origin feature controls enabled");
    }

    public static boolean isAdmin(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel level) || level.getServer() == null) return false;
        return level.getServer().getPlayerList().isOp(new NameAndId(player.getGameProfile()));
    }

    public static void registerSettingsNetworking() {
        ServerPlayNetworking.registerGlobalReceiver(UpdateAdminSettingPayload.TYPE, (payload, context) ->
            context.server().execute(() -> {
                if (isAdmin(context.player())) MerlingConfig.updateSetting(payload.key(), payload.value());
            }));
        ServerPlayNetworking.registerGlobalReceiver(UpdateAdminFeaturePayload.TYPE, (payload, context) ->
            context.server().execute(() -> {
                if (isAdmin(context.player())) set(payload.origin(), payload.feature(), payload.enabled());
            }));
    }

    public static void setFeature(Identifier origin, Identifier feature, boolean enabled) { set(origin, feature, enabled); }

    public static boolean enabled(Player player, Identifier origin, Identifier feature) {
        return player == null || origin == null || feature == null || OVERRIDES.getOrDefault(key(origin, feature), true);
    }

    public static boolean enabledForFeature(Player player, Identifier feature) {
        if (player == null || feature == null) return true;
        for (Identifier origin : OriginsLegacyAdapter.activeOrigins(player)) {
            if (!enabled(player, origin, feature)) return false;
        }
        return true;
    }

    public static boolean powerEnabled(LivingEntity entity, PowerType<?> powerType) {
        if (!(entity instanceof Player player) || powerType == null || powerType.getIdentifier() == null) return true;
        for (Identifier source : PowerHolderComponent.KEY.get(player).getSources(powerType)) {
            if (!enabled(player, source, powerType.getIdentifier())) return false;
        }
        return true;
    }

    private static void set(Identifier origin, Identifier feature, boolean enabled) {
        String key = key(origin, feature);
        if (enabled) OVERRIDES.remove(key);
        else OVERRIDES.put(key, false);
        save();
    }

    private static void resetOrigin(Identifier origin) {
        OVERRIDES.keySet().removeIf(key -> key.startsWith(origin + "|"));
        save();
    }

    private static String key(Identifier origin, Identifier feature) {
        return origin + "|" + feature;
    }

    private static void load() {
        OVERRIDES.clear();
        if (!Files.exists(FILE)) return;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            properties.load(reader);
            for (String key : properties.stringPropertyNames()) {
                if ("false".equalsIgnoreCase(properties.getProperty(key))) OVERRIDES.put(key, false);
            }
        } catch (Exception exception) {
            OriginsReimagined.LOGGER.warn("Could not load admin feature overrides", exception);
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Properties properties = new Properties();
            OVERRIDES.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> properties.setProperty(entry.getKey(), "false"));
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                properties.store(writer, "Origins: Reimagined server feature overrides");
            }
        } catch (Exception exception) {
            OriginsReimagined.LOGGER.warn("Could not save admin feature overrides", exception);
        }
    }
}
