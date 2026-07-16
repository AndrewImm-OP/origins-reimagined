package com.andrewimm.originsreimagined.client.visual.profile;

import com.andrewimm.originsreimagined.client.ClientSelectionConfig;
import com.andrewimm.originsreimagined.client.visual.context.PlayerVisualContext;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;
import com.andrewimm.originsreimagined.client.visual.render.VisualRenderCapability;
import net.minecraft.resources.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class VisualProfileResolver {
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    private VisualProfileResolver() {}
    public static ResolvedVisualProfile resolve(PlayerVisualContext context, boolean enabled) {
        return resolve(context, enabled, Set.of());
    }
    public static ResolvedVisualProfile resolve(PlayerVisualContext context, boolean enabled, Set<VisualRenderCapability> capabilities) {
        if (!enabled || !ClientSelectionConfig.visualsEnabled() || context == null || context.originId() == null) return ResolvedVisualProfile.empty();
        VisualProfile profile = VisualProfileManager.getForOrigin(context.originId()).orElse(null);
        return resolve(profile, context, enabled, capabilities);
    }
    public static ResolvedVisualProfile resolve(Identifier profileId, PlayerVisualContext context, boolean enabled) {
        return resolve(profileId, context, enabled, Set.of());
    }
    public static ResolvedVisualProfile resolve(Identifier profileId, PlayerVisualContext context, boolean enabled, Set<VisualRenderCapability> capabilities) {
        if (!enabled || !ClientSelectionConfig.visualsEnabled() || context == null || profileId == null) return ResolvedVisualProfile.empty();
        VisualProfile profile = VisualProfileManager.get(profileId).orElse(null);
        return resolve(profile, context, enabled, capabilities);
    }

    private static ResolvedVisualProfile resolve(VisualProfile profile, PlayerVisualContext context, boolean enabled, Set<VisualRenderCapability> capabilities) {
        if (!enabled || !ClientSelectionConfig.visualsEnabled() || context == null) return ResolvedVisualProfile.empty();
        if (profile == null) return ResolvedVisualProfile.empty();
        List<VisualModifier> active = new ArrayList<>();
        List<Identifier> unsupported = new ArrayList<>();
        for (VisualModifier modifier : profile.modifiers()) {
            if (!modifier.condition().matches(context)) continue;
            VisualRenderCapability capability = capability(modifier.type().getPath());
            if (capabilities.isEmpty() || capability == null || (capabilities.contains(capability) && enabledByConfig(capability))) active.add(modifier);
            else {
                unsupported.add(Identifier.fromNamespaceAndPath(profile.profileId().getNamespace(), profile.profileId().getPath() + "/" + modifier.id()));
                String key = profile.profileId() + ":" + modifier.id() + ":" + capability;
                if (WARNED.add(key)) com.andrewimm.originsreimagined.OriginsReimagined.LOGGER.warn("Visual modifier {} in {} is unsupported by the active render backend ({})", modifier.id(), profile.profileId(), capability);
            }
        }
        active.sort(java.util.Comparator.comparing(VisualModifier::renderPhase));
        return new ResolvedVisualProfile(profile, active, unsupported);
    }
    private static boolean enabledByConfig(VisualRenderCapability capability) {
        return switch (capability) {
            case MODEL_TINT -> true;
            case TEXTURE_OVERLAY -> ClientSelectionConfig.visualOverlays();
            case EMISSIVE_OVERLAY -> ClientSelectionConfig.visualEmissive();
            case MODEL_ALPHA -> false;
            case GEOMETRY_ATTACHMENT -> ClientSelectionConfig.visualAttachments();
            case PARTICLE_AURA -> ClientSelectionConfig.visualParticles();
        };
    }
    private static VisualRenderCapability capability(String type) {
        return switch (type) {
            case "model_tint" -> VisualRenderCapability.MODEL_TINT;
            case "texture_overlay" -> VisualRenderCapability.TEXTURE_OVERLAY;
            case "emissive_overlay", "eye_overlay" -> VisualRenderCapability.EMISSIVE_OVERLAY;
            case "model_alpha" -> VisualRenderCapability.MODEL_ALPHA;
            case "geometry_attachment" -> VisualRenderCapability.GEOMETRY_ATTACHMENT;
            case "particle_aura" -> VisualRenderCapability.PARTICLE_AURA;
            default -> null;
        };
    }
}
