package ru.origins_overhaul.model;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public record PowerData(Identifier id, Component name, Component description, boolean hidden) {}
