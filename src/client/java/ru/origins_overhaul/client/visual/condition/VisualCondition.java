package ru.origins_overhaul.client.visual.condition;

import ru.origins_overhaul.client.visual.context.PlayerVisualContext;

public record VisualCondition(String type, String power) {
    public static VisualCondition always() { return new VisualCondition("always", null); }

    public boolean matches(PlayerVisualContext context) {
        if (context == null) return false;
        return switch (type == null ? "always" : type) {
            case "always" -> true;
            case "preview" -> context.preview();
            case "world" -> !context.preview();
            case "local_player" -> context.localPlayer();
            case "power_active" -> power != null && context.powerActive(net.minecraft.resources.Identifier.tryParse(power));
            case "power_inactive" -> power == null || !context.powerActive(net.minecraft.resources.Identifier.tryParse(power));
            case "in_water", "underwater" -> context.inWater();
            case "on_fire" -> context.onFire();
            case "sneaking" -> context.sneaking();
            case "swimming" -> context.swimming();
            case "fall_flying" -> context.fallFlying();
            default -> false;
        };
    }
}
