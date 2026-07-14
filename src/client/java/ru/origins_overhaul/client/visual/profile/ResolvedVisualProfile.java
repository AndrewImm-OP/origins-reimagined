package ru.origins_overhaul.client.visual.profile;

import ru.origins_overhaul.client.visual.modifier.VisualModifier;
import java.util.List;
import net.minecraft.resources.Identifier;

public record ResolvedVisualProfile(VisualProfile profile, List<VisualModifier> modifiers, List<Identifier> unsupportedModifiers) {
    public ResolvedVisualProfile {
        modifiers = List.copyOf(modifiers == null ? List.of() : modifiers);
        unsupportedModifiers = List.copyOf(unsupportedModifiers == null ? List.of() : unsupportedModifiers);
    }
    public ResolvedVisualProfile(VisualProfile profile, List<VisualModifier> modifiers) { this(profile, modifiers, List.of()); }
    public static ResolvedVisualProfile empty() { return new ResolvedVisualProfile(null, List.of(), List.of()); }
    public boolean isEmpty() { return modifiers.isEmpty(); }
}
