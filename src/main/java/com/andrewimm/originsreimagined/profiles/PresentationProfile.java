package com.andrewimm.originsreimagined.profiles;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public record PresentationProfile(
        Identifier originId,
        Integer customAccentColor,
        List<Entry> advantages,
        List<Entry> disadvantages,
        List<Entry> neutralFeatures,
        List<Identifier> hiddenPowers,
        Identifier visualProfile,
        Identifier relationProfile
) {
    public PresentationProfile {
        advantages = List.copyOf(advantages);
        disadvantages = List.copyOf(disadvantages);
        neutralFeatures = List.copyOf(neutralFeatures);
        hiddenPowers = List.copyOf(hiddenPowers);
    }

    public record Entry(List<Identifier> powers, Component name, Component description, int order) {
        public Entry {
            powers = List.copyOf(powers);
        }
    }
}
