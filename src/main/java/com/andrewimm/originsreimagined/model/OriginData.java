package com.andrewimm.originsreimagined.model;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record OriginData(
        Identifier id,
        Identifier layerId,
        Component name,
        ItemStack icon,
        int order,
        int impact,
        boolean choosable,
        List<PowerData> powers
) {
    public OriginData {
        icon = icon.copy();
        powers = List.copyOf(powers);
    }
}
