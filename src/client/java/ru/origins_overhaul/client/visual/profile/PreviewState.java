package ru.origins_overhaul.client.visual.profile;

import net.minecraft.network.chat.Component;
import java.util.List;

public record PreviewState(String id, Component name, List<String> simulatePowers) {
    public PreviewState { simulatePowers = List.copyOf(simulatePowers == null ? List.of() : simulatePowers); }
}
