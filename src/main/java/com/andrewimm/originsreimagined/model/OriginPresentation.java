package com.andrewimm.originsreimagined.model;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record OriginPresentation(
        Identifier originId,
        Identifier layerId,
        Component name,
        ItemStack icon,
        int order,
        int impact,
        List<PresentedPower> advantages,
        List<PresentedPower> disadvantages,
        List<PresentedPower> neutralFeatures,
        Optional<Identifier> visualProfileId,
        Optional<Identifier> relationProfileId,
        Optional<Integer> customAccentColor,
        PresentationSource source
) {
    public OriginPresentation {
        icon = icon.copy();
        advantages = List.copyOf(advantages);
        disadvantages = List.copyOf(disadvantages);
        neutralFeatures = List.copyOf(neutralFeatures);
    }
}
