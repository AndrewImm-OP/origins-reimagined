package ru.origins_overhaul.client.visual.profile;

import ru.origins_overhaul.client.visual.modifier.VisualModifier;
import java.util.List;

public record ResolvedVisualProfile(VisualProfile profile, List<VisualModifier> modifiers) {
    public ResolvedVisualProfile { modifiers = List.copyOf(modifiers); }
    public static ResolvedVisualProfile empty() { return new ResolvedVisualProfile(null, List.of()); }
    public boolean isEmpty() { return modifiers.isEmpty(); }
}
