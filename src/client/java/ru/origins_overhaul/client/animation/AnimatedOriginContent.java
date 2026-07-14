package ru.origins_overhaul.client.animation;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import ru.origins_overhaul.model.OriginPresentation;
import ru.origins_overhaul.model.PresentedPower;

import java.util.HashMap;
import java.util.Map;

public final class AnimatedOriginContent {
    private final Map<Identifier, AnimatedPowerContent> powers;

    private AnimatedOriginContent(Map<Identifier, AnimatedPowerContent> powers) { this.powers = Map.copyOf(powers); }

    public static AnimatedOriginContent create(Font font, OriginPresentation origin, int width) {
        Map<Identifier, AnimatedPowerContent> result = new HashMap<>();
        origin.advantages().forEach(power -> result.put(key(power), AnimatedPowerContent.create(font, power, width)));
        origin.disadvantages().forEach(power -> result.put(key(power), AnimatedPowerContent.create(font, power, width)));
        origin.neutralFeatures().forEach(power -> result.put(key(power), AnimatedPowerContent.create(font, power, width)));
        return new AnimatedOriginContent(result);
    }

    public AnimatedPowerContent power(Identifier powerId) { return powers.getOrDefault(powerId, new AnimatedPowerContent(java.util.List.of())); }
    private static Identifier key(PresentedPower power) { return power.powerId(); }
}
