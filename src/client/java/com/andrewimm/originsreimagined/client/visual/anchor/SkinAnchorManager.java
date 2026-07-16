package com.andrewimm.originsreimagined.client.visual.anchor;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public final class SkinAnchorManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, EyeAnchorProfile> profiles = new HashMap<>();
    private static Path file;
    private SkinAnchorManager() {}

    public static void load(Path gameDirectory) {
        file = gameDirectory.resolve("config/origins_reimagined/skin_anchors.json");
        profiles.clear();
        if (!Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has("skins")) for (var entry : root.getAsJsonObject("skins").entrySet()) profiles.put(entry.getKey(), parse(entry.getValue().getAsJsonObject()));
        } catch (Exception exception) { OriginsReimagined.LOGGER.warn("Unable to load skin anchors: {}", exception.getMessage()); }
    }

    public static EyeAnchorProfile get(PlayerAppearanceSnapshot appearance) { return profiles.getOrDefault(hash(appearance), EyeAnchorProfile.preset(EyePreset.STANDARD)); }
    public static String hash(PlayerAppearanceSnapshot appearance) { return "sha256:" + digest(appearance.skinTexture().toString()); }
    public static void put(PlayerAppearanceSnapshot appearance, EyeAnchorProfile profile) { profiles.put(hash(appearance), profile); save(); }

    private static EyeAnchorProfile parse(JsonObject json) {
        EyePreset preset; try { preset = EyePreset.valueOf(json.has("eye_preset") ? json.get("eye_preset").getAsString().toUpperCase() : "STANDARD"); } catch (RuntimeException e) { preset = EyePreset.STANDARD; }
        return new EyeAnchorProfile(preset, eye(json.get("left_eye"), preset.left()), eye(json.get("right_eye"), preset.right()));
    }
    private static EyeAnchor eye(JsonElement element, EyeAnchor fallback) { if (element == null || !element.isJsonObject()) return fallback; JsonObject json = element.getAsJsonObject(); EyeLayer layer; try { layer = EyeLayer.valueOf(json.has("layer") ? json.get("layer").getAsString().toUpperCase() : "BASE"); } catch (RuntimeException e) { layer = EyeLayer.BASE; } return new EyeAnchor(integer(json,"x",fallback.x()), integer(json,"y",fallback.y()), integer(json,"width",fallback.width()), integer(json,"height",fallback.height()), layer); }
    private static int integer(JsonObject json, String key, int fallback) { try { return json.has(key) ? json.get(key).getAsInt() : fallback; } catch (RuntimeException e) { return fallback; } }
    private static void save() { if (file == null) return; try { Files.createDirectories(file.getParent()); JsonObject root = new JsonObject(); root.add("skins", new JsonObject()); for (var entry : profiles.entrySet()) root.getAsJsonObject("skins").add(entry.getKey(), write(entry.getValue())); try (Writer writer = Files.newBufferedWriter(file)) { GSON.toJson(root, writer); } } catch (Exception exception) { OriginsReimagined.LOGGER.warn("Unable to save skin anchors: {}", exception.getMessage()); } }
    private static JsonObject write(EyeAnchorProfile profile) { JsonObject json = new JsonObject(); json.addProperty("eye_preset", profile.preset().name()); json.add("left_eye", write(profile.leftEye())); json.add("right_eye", write(profile.rightEye())); return json; }
    private static JsonObject write(EyeAnchor eye) { JsonObject json = new JsonObject(); json.addProperty("x", eye.x()); json.addProperty("y", eye.y()); json.addProperty("width", eye.width()); json.addProperty("height", eye.height()); json.addProperty("layer", eye.layer().name()); return json; }
    private static String digest(String value) { try { byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder(); for (byte b : bytes) result.append(String.format("%02x", b)); return result.toString(); } catch (Exception e) { return Integer.toHexString(value.hashCode()); } }
}
