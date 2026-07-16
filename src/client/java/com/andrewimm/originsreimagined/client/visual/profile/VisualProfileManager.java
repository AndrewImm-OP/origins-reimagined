package com.andrewimm.originsreimagined.client.visual.profile;

import com.google.gson.*;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.client.visual.condition.VisualCondition;
import com.andrewimm.originsreimagined.client.visual.modifier.RenderPhase;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;

import java.util.*;

public final class VisualProfileManager extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final VisualProfileManager INSTANCE = new VisualProfileManager();
    private static volatile Map<Identifier, VisualProfile> profiles = Map.of();
    private static volatile Map<Identifier, VisualProfile> byOrigin = Map.of();
    private static volatile Runnable reloadCallback = () -> {};

    private VisualProfileManager() { super(GSON, "visual_profiles"); }
    @Override public Identifier getFabricId() { return Identifier.fromNamespaceAndPath("origins_reimagined", "visual_profiles"); }

    @Override protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, VisualProfile> next = new HashMap<>();
        loader.forEach((resourceId, values) -> values.forEach(json -> {
            try {
                VisualProfile profile = parse(resourceId, json.getAsJsonObject());
                profile.modifiers().forEach(modifier -> {
                    if (modifier.texture() != null && manager.getResource(modifier.texture()).isEmpty()) {
                        OriginsReimagined.LOGGER.warn("Missing texture {} referenced by visual profile {} modifier {}", modifier.texture(), profile.profileId(), modifier.id());
                    }
                });
                VisualProfile previous = next.put(profile.profileId(), profile);
                if (previous != null && profile.priority() < previous.priority()) next.put(profile.profileId(), previous);
            } catch (RuntimeException exception) {
                OriginsReimagined.LOGGER.warn("Ignoring invalid visual profile {}: {}", resourceId, exception.getMessage());
            }
        }));
        profiles = Map.copyOf(next);
        Map<Identifier, VisualProfile> origins = new HashMap<>();
        for (VisualProfile profile : next.values()) {
            VisualProfile previous = origins.get(profile.originId());
            if (previous == null || profile.priority() >= previous.priority()) origins.put(profile.originId(), profile);
        }
        byOrigin = Map.copyOf(origins);
        OriginsReimagined.LOGGER.debug("Loaded {} Origins: Reimagined visual profiles", profiles.size());
        reloadCallback.run();
    }

    public static void setReloadCallback(Runnable callback) { reloadCallback = callback == null ? () -> {} : callback; }
    public static Optional<VisualProfile> get(Identifier originId) { return Optional.ofNullable(profiles.get(originId)); }
    public static Optional<VisualProfile> getForOrigin(Identifier originId) { return Optional.ofNullable(byOrigin.get(originId)); }
    public static Map<Identifier, VisualProfile> snapshot() { return profiles; }

    private static VisualProfile parse(Identifier resourceId, JsonObject json) {
        Identifier origin = id(json, "origin").orElseThrow(() -> new JsonParseException("missing origin"));
        int priority = integer(json, "priority", 0);
        List<VisualModifier> modifiers = new ArrayList<>();
        if (json.has("modifiers") && json.get("modifiers").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("modifiers")) {
                try { modifiers.add(parseModifier(resourceId, element.getAsJsonObject(), modifiers.size())); }
                catch (RuntimeException exception) { OriginsReimagined.LOGGER.warn("Skipping modifier in visual profile {}: {}", resourceId, exception.getMessage()); }
            }
        }
        List<PreviewState> states = new ArrayList<>();
        if (json.has("preview_states") && json.get("preview_states").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("preview_states")) {
                JsonObject state = element.getAsJsonObject();
                String stateId = state.has("id") ? state.get("id").getAsString() : "state_" + states.size();
                states.add(new PreviewState(stateId, text(state.get("name"), Component.literal(stateId)), strings(state, "simulate_powers")));
            }
        }
        Identifier profileId = id(json, "id").orElse(resourceId);
        return new VisualProfile(profileId, origin, priority, modifiers, states);
    }

    private static VisualModifier parseModifier(Identifier resourceId, JsonObject json, int index) {
        Identifier type = id(json, "type").orElseThrow(() -> new JsonParseException("missing modifier type"));
        Identifier texture = id(json, "texture").orElse(null);
        int color = color(json.has("color") ? json.get("color").getAsString() : "#FFFFFF", resourceId);
        float opacity = clamp(number(json, "opacity", 1.0f), 0.0f, 1.0f);
        float strength = clamp(number(json, "strength", 1.0f), 0.0f, 1.0f);
        String anchor = json.has("anchor") ? json.get("anchor").getAsString() : null;
        VisualCondition condition = parseCondition(json.get("condition"));
        RenderPhase phase = renderPhase(json.has("render_phase") ? json.get("render_phase").getAsString() : null);
        String modifierId = json.has("id") && json.get("id").isJsonPrimitive() ? json.get("id").getAsString() : type.getPath() + "_" + index;
        float[] size = vector(json, "size", new float[]{1, 1, 1});
        int[] uv = integerVector(json, "uv", new int[]{0, 0});
        JsonObject geometry = json.has("geometry") && json.get("geometry").isJsonObject() ? json.getAsJsonObject("geometry") : null;
        String geometryType = geometry != null && geometry.has("type") ? geometry.get("type").getAsString() : json.has("geometry_type") ? json.get("geometry_type").getAsString() : "cuboid";
        if (geometry != null) { size = vector(geometry, "size", size); uv = integerVector(geometry, "uv", uv); texture = id(geometry, "texture").orElse(texture); }
        JsonObject animation = json.has("animation") && json.get("animation").isJsonObject() ? json.getAsJsonObject("animation") : null;
        String animationType = animation != null && animation.has("type") ? animation.get("type").getAsString() : "STATIC";
        float animationAmplitude = animation == null ? 0 : number(animation, "amplitude", 0);
        float animationSpeed = animation == null ? 0 : number(animation, "speed", 0);
        float animationWalkMultiplier = animation == null ? 0 : number(animation, "walk_multiplier", 0);
        Identifier particle = id(json, "particle").orElse(null);
        float particleRate = number(json, "rate", 0);
        float particleRadius = number(json, "radius", 0.45f);
        float particleHeight = number(json, "height", 1.7f);
        int segmentCount = integer(json, "segments", 1);
        float[] segmentOffset = vector(json, "segment_offset", new float[]{0, 0.18f, 0});
        float[] baseRotation = vector(json, "base_rotation", new float[]{0, 0, 0});
        float bendYaw = 0, bendPitch = 0;
        if (json.has("bend") && json.get("bend").isJsonObject()) { JsonObject bend = json.getAsJsonObject("bend"); bendYaw = number(bend, "yaw", 0); bendPitch = number(bend, "pitch", 0); }
        if (geometry != null) { segmentCount = integer(geometry, "segments", segmentCount); segmentOffset = vector(geometry, "segment_offset", segmentOffset); baseRotation = vector(geometry, "base_rotation", baseRotation); if (geometry.has("bend") && geometry.get("bend").isJsonObject()) { JsonObject bend = geometry.getAsJsonObject("bend"); bendYaw = number(bend, "yaw", bendYaw); bendPitch = number(bend, "pitch", bendPitch); } }
        return new VisualModifier(modifierId, type, texture, color, opacity, strength, strings(json, "parts"), anchor, condition, phase,
            vector(json, "offset", new float[]{0, 0, 0}), vector(json, "rotation", new float[]{0, 0, 0}), vector(json, "scale", new float[]{1, 1, 1}),
            bool(json, "hide_when_head_armor", false), bool(json, "hide_when_chest_armor", false), geometryType, size, uv,
            bool(json, "mirror", false), animationType, animationAmplitude, animationSpeed, animationWalkMultiplier, particle, particleRate, particleRadius, particleHeight, segmentCount, segmentOffset, baseRotation, bendYaw, bendPitch);
    }

    private static VisualCondition parseCondition(JsonElement element) {
        if (element == null || !element.isJsonObject()) return VisualCondition.always();
        JsonObject json = element.getAsJsonObject();
        return new VisualCondition(json.has("type") ? json.get("type").getAsString() : "always", json.has("power") ? json.get("power").getAsString() : null);
    }
    private static RenderPhase renderPhase(String value) { try { return value == null ? RenderPhase.AFTER_OUTER_LAYER : RenderPhase.valueOf(value.toUpperCase(Locale.ROOT)); } catch (RuntimeException e) { return RenderPhase.AFTER_OUTER_LAYER; } }
    private static Optional<Identifier> id(JsonObject json, String key) { return json.has(key) && json.get(key).isJsonPrimitive() ? Optional.ofNullable(Identifier.tryParse(json.get(key).getAsString())) : Optional.empty(); }
    private static List<String> strings(JsonObject json, String key) { if (!json.has(key) || !json.get(key).isJsonArray()) return List.of(); List<String> result = new ArrayList<>(); for (JsonElement e : json.getAsJsonArray(key)) if (e.isJsonPrimitive()) result.add(e.getAsString()); return result; }
    private static int integer(JsonObject json, String key, int fallback) { try { return json.has(key) ? json.get(key).getAsInt() : fallback; } catch (RuntimeException e) { return fallback; } }
    private static float number(JsonObject json, String key, float fallback) { try { return json.has(key) ? json.get(key).getAsFloat() : fallback; } catch (RuntimeException e) { return fallback; } }
    private static boolean bool(JsonObject json, String key, boolean fallback) { return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsBoolean() : fallback; }
    private static float[] vector(JsonObject json, String key, float[] fallback) { if (!json.has(key) || !json.get(key).isJsonArray() || json.getAsJsonArray(key).size() != 3) return fallback; float[] result = new float[3]; for (int i = 0; i < 3; i++) result[i] = number(json.getAsJsonArray(key), i, fallback[i]); return result; }
    private static float number(JsonArray array, int index, float fallback) { try { return array.get(index).getAsFloat(); } catch (RuntimeException e) { return fallback; } }
    private static int[] integerVector(JsonObject json, String key, int[] fallback) { if (!json.has(key) || !json.get(key).isJsonArray() || json.getAsJsonArray(key).size() != 2) return fallback; int[] result = new int[2]; for (int i = 0; i < 2; i++) { try { result[i] = json.getAsJsonArray(key).get(i).getAsInt(); } catch (RuntimeException e) { result[i] = fallback[i]; } } return result; }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    private static int color(String value, Identifier resource) { try { String hex = value.startsWith("#") ? value.substring(1) : value; if (hex.length() != 6 && hex.length() != 8) throw new NumberFormatException(); return (int) Long.parseLong(hex, 16) | (hex.length() == 6 ? 0xFF000000 : 0); } catch (RuntimeException e) { OriginsReimagined.LOGGER.warn("Invalid visual color '{}' in {}", value, resource); return 0xFFFFFFFF; } }
    private static Component text(JsonElement element, Component fallback) { if (element == null || !element.isJsonObject()) return fallback; JsonObject json = element.getAsJsonObject(); if (json.has("translate")) return Component.translatable(json.get("translate").getAsString()); if (json.has("text")) return Component.literal(json.get("text").getAsString()); return fallback; }
}
