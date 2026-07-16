package com.andrewimm.originsreimagined.client.visual.profile;

import net.minecraft.resources.Identifier;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;
import java.util.List;

public record VisualProfile(Identifier profileId, Identifier originId, int priority, List<VisualModifier> modifiers, List<PreviewState> previewStates) {
    public VisualProfile { modifiers = List.copyOf(modifiers); previewStates = List.copyOf(previewStates); }
}
