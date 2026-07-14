package ru.origins_overhaul.model;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public record PresentedPower(
        Identifier powerId,
        Component name,
        Component description,
        PowerPresentationCategory category,
        int order,
        boolean technical,
        List<Identifier> representedPowerIds
) {
    public PresentedPower {
        representedPowerIds = List.copyOf(representedPowerIds);
    }
}
