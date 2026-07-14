package ru.origins_overhaul.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import ru.origins_overhaul.OriginsOverhaul;
import ru.origins_overhaul.compat.originslegacy.OriginsLegacyAdapter;
import ru.origins_overhaul.model.*;
import ru.origins_overhaul.profiles.OriginDifficultyColorResolver;
import ru.origins_overhaul.profiles.PresentationProfile;
import ru.origins_overhaul.profiles.PresentationProfileManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class ClientOriginCatalog {
    private static final AtomicReference<CatalogSnapshot> CURRENT = new AtomicReference<>(CatalogSnapshot.empty());

    private ClientOriginCatalog() {}

    public static void rebuild() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            clear();
            return;
        }
        long start = System.nanoTime();
        List<OriginLayerSnapshot> layers = OriginsLegacyAdapter.layers();
        Map<Identifier, List<OriginPresentation>> origins = new LinkedHashMap<>();
        for (OriginLayerSnapshot layer : layers) {
            List<OriginPresentation> presentations = new ArrayList<>();
            for (OriginData data : OriginsLegacyAdapter.origins(layer, client.player)) {
                presentations.add(present(data));
            }
            presentations.sort(Comparator.comparingInt(OriginPresentation::impact).thenComparingInt(OriginPresentation::order).thenComparing(p -> p.originId().toString()));
            origins.put(layer.id(), List.copyOf(presentations));
        }
        CURRENT.set(new CatalogSnapshot(layers, origins));
        if (OriginsOverhaul.DEBUG) {
            OriginsOverhaul.LOGGER.info("Rebuilt origin catalog: {} layers, {} origins in {} ms", layers.size(), origins.values().stream().mapToInt(List::size).sum(), (System.nanoTime() - start) / 1_000_000.0);
        }
    }

    public static void clear() {
        CURRENT.set(CatalogSnapshot.empty());
    }

    public static List<OriginLayerSnapshot> getLayers() {
        return CURRENT.get().layers();
    }

    public static List<OriginPresentation> getOrigins(Identifier layerId) {
        return CURRENT.get().origins().getOrDefault(layerId, List.of());
    }

    public static Optional<OriginPresentation> getOrigin(Identifier layerId, Identifier originId) {
        return getOrigins(layerId).stream().filter(origin -> origin.originId().equals(originId)).findFirst();
    }

    public static int layerCount() {
        return CURRENT.get().layers().size();
    }

    public static int originCount() {
        return CURRENT.get().origins().values().stream().mapToInt(List::size).sum();
    }

    private static OriginPresentation present(OriginData data) {
        Optional<PresentationProfile> profile = PresentationProfileManager.get(data.id());
        if (profile.isEmpty()) return fallback(data);
        return merge(data, profile.get());
    }

    private static OriginPresentation fallback(OriginData data) {
        List<PresentedPower> neutral = new ArrayList<>();
        for (PowerData power : data.powers()) {
            neutral.add(new PresentedPower(power.id(), safe(power.name(), power.id()), safe(power.description(), power.id()), PowerPresentationCategory.NEUTRAL, neutral.size(), power.hidden(), List.of(power.id())));
        }
        return new OriginPresentation(data.id(), data.layerId(), safe(data.name(), data.id()), data.icon(), data.order(), data.impact(), List.of(), List.of(), neutral, Optional.empty(), Optional.empty(), Optional.empty(), PresentationSource.AUTOMATIC_FALLBACK);
    }

    private static OriginPresentation merge(OriginData data, PresentationProfile profile) {
        Map<Identifier, PowerData> real = new HashMap<>();
        data.powers().forEach(power -> real.put(power.id(), power));
        Set<Identifier> consumed = new HashSet<>();
        Set<Identifier> hidden = new HashSet<>(profile.hiddenPowers());
        List<PresentedPower> advantages = entries(profile.advantages(), PowerPresentationCategory.ADVANTAGE, real, consumed, hidden, data.id());
        List<PresentedPower> disadvantages = entries(profile.disadvantages(), PowerPresentationCategory.DISADVANTAGE, real, consumed, hidden, data.id());
        List<PresentedPower> neutral = entries(profile.neutralFeatures(), PowerPresentationCategory.NEUTRAL, real, consumed, hidden, data.id());
        for (PowerData power : data.powers()) {
            if (hidden.contains(power.id())) continue;
            if (!consumed.contains(power.id())) {
                neutral.add(new PresentedPower(power.id(), safe(power.name(), power.id()), safe(power.description(), power.id()), PowerPresentationCategory.NEUTRAL, neutral.size(), power.hidden(), List.of(power.id())));
            }
        }
        neutral.sort(Comparator.comparingInt(PresentedPower::order).thenComparing(power -> power.powerId().toString()));
        return new OriginPresentation(data.id(), data.layerId(), safe(data.name(), data.id()), data.icon(), data.order(), data.impact(), advantages, disadvantages, neutral, Optional.ofNullable(profile.visualProfile()), Optional.ofNullable(profile.relationProfile()), Optional.ofNullable(profile.customAccentColor()).map(color -> OriginDifficultyColorResolver.resolve(data.impact(), color)), PresentationProfileManager.isBuiltin(profile.originId()) ? PresentationSource.BUILTIN_PROFILE : PresentationSource.RESOURCE_PROFILE);
    }

    private static List<PresentedPower> entries(List<PresentationProfile.Entry> entries, PowerPresentationCategory category, Map<Identifier, PowerData> real, Set<Identifier> consumed, Set<Identifier> hidden, Identifier originId) {
        List<PresentedPower> result = new ArrayList<>();
        for (PresentationProfile.Entry entry : entries) {
            List<Identifier> valid = new ArrayList<>();
            for (Identifier powerId : entry.powers()) {
                if (hidden.contains(powerId)) continue;
                if (real.containsKey(powerId)) {
                    valid.add(powerId);
                    consumed.add(powerId);
                } else {
                    OriginsOverhaul.LOGGER.warn("Presentation profile for {} references missing power {}", originId, powerId);
                }
            }
            if (!valid.isEmpty()) {
                result.add(new PresentedPower(valid.get(0), entry.name(), entry.description(), category, entry.order(), false, valid));
            }
        }
        result.sort(Comparator.comparingInt(PresentedPower::order));
        return result;
    }

    private static Component safe(Component component, Identifier fallback) {
        return component == null ? Component.literal(displayName(fallback)) : component;
    }

    private static String displayName(Identifier id) {
        String path = id.getPath().replace('_', ' ').replace('-', ' ');
        StringBuilder result = new StringBuilder();
        for (String word : path.split(" ")) {
            if (word.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.isEmpty() ? id.toString() : result.toString();
    }

    private record CatalogSnapshot(List<OriginLayerSnapshot> layers, Map<Identifier, List<OriginPresentation>> origins) {
        private CatalogSnapshot {
            layers = List.copyOf(layers);
            origins = Map.copyOf(origins);
        }

        private static CatalogSnapshot empty() {
            return new CatalogSnapshot(List.of(), Map.of());
        }
    }
}
