package ru.origins_overhaul.client.visual.profile;

import ru.origins_overhaul.client.ClientSelectionConfig;
import ru.origins_overhaul.client.visual.context.PlayerVisualContext;
import ru.origins_overhaul.client.visual.modifier.VisualModifier;
import net.minecraft.resources.Identifier;

public final class VisualProfileResolver {
    private VisualProfileResolver() {}
    public static ResolvedVisualProfile resolve(PlayerVisualContext context, boolean enabled) {
        if (!enabled || !ClientSelectionConfig.visualsEnabled() || context == null || context.originId() == null) return ResolvedVisualProfile.empty();
        VisualProfile profile = VisualProfileManager.getForOrigin(context.originId()).orElse(null);
        return resolve(profile, context, enabled);
    }
    public static ResolvedVisualProfile resolve(Identifier profileId, PlayerVisualContext context, boolean enabled) {
        if (!enabled || !ClientSelectionConfig.visualsEnabled() || context == null || profileId == null) return ResolvedVisualProfile.empty();
        VisualProfile profile = VisualProfileManager.get(profileId).orElse(null);
        return resolve(profile, context, enabled);
    }

    private static ResolvedVisualProfile resolve(VisualProfile profile, PlayerVisualContext context, boolean enabled) {
        if (!enabled || !ClientSelectionConfig.visualsEnabled() || context == null) return ResolvedVisualProfile.empty();
        if (profile == null) return ResolvedVisualProfile.empty();
        return new ResolvedVisualProfile(profile, profile.modifiers().stream()
            .filter(modifier -> modifier.condition().matches(context))
            .sorted(java.util.Comparator.comparing(VisualModifier::renderPhase))
            .toList());
    }
}
