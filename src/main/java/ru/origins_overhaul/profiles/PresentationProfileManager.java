package ru.origins_overhaul.profiles;

import com.google.gson.*;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import ru.origins_overhaul.OriginsOverhaul;

import java.util.*;

public final class PresentationProfileManager extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final PresentationProfileManager INSTANCE = new PresentationProfileManager();
    private static volatile Map<Identifier, PresentationProfile> profiles = Map.of();
    private static volatile Set<Identifier> builtinOrigins = Set.of();
    private static volatile Runnable reloadCallback = () -> {};

    private PresentationProfileManager() {
        super(GSON, "presentations");
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath("origins_overhaul", "presentations");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, PresentationProfile> next = new HashMap<>();
        Set<Identifier> builtin = new HashSet<>();
        loader.forEach((id, values) -> {
            for (JsonElement element : values) {
                try {
                    PresentationProfile profile = parse(id, element.getAsJsonObject());
                    PresentationProfile previous = next.put(profile.originId(), profile);
                    if (id.getNamespace().equals("origins_overhaul")) builtin.add(profile.originId());
                    if (previous != null) {
                        OriginsOverhaul.LOGGER.warn("Multiple presentation profiles resolved for {}; using later resource {}", profile.originId(), id);
                    }
                } catch (RuntimeException exception) {
                    OriginsOverhaul.LOGGER.warn("Ignoring invalid Origins Overhaul presentation profile {}: {}", id, exception.getMessage());
                }
            }
        });
        profiles = Map.copyOf(next);
        builtinOrigins = Set.copyOf(builtin);
        OriginsOverhaul.LOGGER.debug("Loaded {} Origins Overhaul presentation profiles", profiles.size());
        reloadCallback.run();
    }

    public static void setReloadCallback(Runnable callback) {
        reloadCallback = callback == null ? () -> {} : callback;
    }

    public static Optional<PresentationProfile> get(Identifier originId) {
        return Optional.ofNullable(profiles.get(originId));
    }

    public static Map<Identifier, PresentationProfile> snapshot() {
        return profiles;
    }

    public static boolean isBuiltin(Identifier originId) {
        return builtinOrigins.contains(originId);
    }

    private static PresentationProfile parse(Identifier resourceId, JsonObject json) {
        Identifier originId = identifier(json, "origin").orElseThrow(() -> new JsonParseException("missing origin"));
        Integer color = null;
        JsonObject difficulty = object(json, "difficulty");
        if (difficulty != null && difficulty.has("color")) {
            color = parseColor(difficulty.get("color").getAsString(), resourceId);
        }
        List<PresentationProfile.Entry> advantages = entries(json, "advantages", resourceId);
        List<PresentationProfile.Entry> disadvantages = entries(json, "disadvantages", resourceId);
        List<PresentationProfile.Entry> neutral = entries(json, "neutral_features", resourceId);
        List<Identifier> hidden = identifiers(json, "hidden_powers");
        Identifier visual = identifier(json, "visual_profile").orElse(null);
        Identifier relation = identifier(json, "relation_profile").orElse(null);
        return new PresentationProfile(originId, color, advantages, disadvantages, neutral, hidden, visual, relation);
    }

    private static List<PresentationProfile.Entry> entries(JsonObject root, String key, Identifier resourceId) {
        List<PresentationProfile.Entry> result = new ArrayList<>();
        JsonArray array = root.has(key) && root.get(key).isJsonArray() ? root.getAsJsonArray(key) : new JsonArray();
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            List<Identifier> powers = identifiers(object, "powers");
            if (powers.isEmpty()) {
                OriginsOverhaul.LOGGER.warn("Profile {} has an empty {} entry", resourceId, key);
                continue;
            }
            Component name = text(object.get("name"), Component.literal(powers.get(0).toString()));
            Component description = text(object.get("description"), Component.literal("No description available."));
            int order = object.has("order") ? object.get("order").getAsInt() : result.size();
            result.add(new PresentationProfile.Entry(powers, name, description, order));
        }
        return result;
    }

    private static List<Identifier> identifiers(JsonObject object, String key) {
        List<Identifier> result = new ArrayList<>();
        if (!object.has(key) || !object.get(key).isJsonArray()) return result;
        for (JsonElement element : object.getAsJsonArray(key)) {
            try {
                Identifier id = Identifier.tryParse(element.getAsString());
                if (id != null) result.add(id);
            } catch (RuntimeException ignored) {
                // Invalid IDs are deliberately ignored; the containing profile remains usable.
            }
        }
        return result;
    }

    private static Optional<Identifier> identifier(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) return Optional.empty();
        Identifier value = Identifier.tryParse(object.get(key).getAsString());
        return Optional.ofNullable(value);
    }

    private static JsonObject object(JsonObject root, String key) {
        return root.has(key) && root.get(key).isJsonObject() ? root.getAsJsonObject(key) : null;
    }

    private static Component text(JsonElement element, Component fallback) {
        if (element == null || !element.isJsonObject()) return element != null && element.isJsonPrimitive() ? Component.literal(element.getAsString()) : fallback;
        JsonObject object = element.getAsJsonObject();
        if (object.has("translate")) return Component.translatable(object.get("translate").getAsString());
        if (object.has("text")) return Component.literal(object.get("text").getAsString());
        return fallback;
    }

    private static int parseColor(String value, Identifier resourceId) {
        try {
            String hex = value.startsWith("#") ? value.substring(1) : value;
            if (hex.length() != 6 && hex.length() != 8) throw new NumberFormatException();
            return (int) Long.parseLong(hex, 16) | (hex.length() == 6 ? 0xFF000000 : 0);
        } catch (RuntimeException exception) {
            OriginsOverhaul.LOGGER.warn("Invalid accent color '{}' in profile {}", value, resourceId);
            return 0;
        }
    }
}
