package ru.origins_overhaul;

import io.github.apace100.origins.Origins;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.origins_overhaul.profiles.PresentationProfileManager;

public final class OriginsOverhaul implements ModInitializer {
    public static final String MOD_ID = "origins_overhaul";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean DEBUG = Boolean.getBoolean("origins_overhaul.debug");

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(PresentationProfileManager.INSTANCE);
        LOGGER.info("Origins Overhaul initialized for Minecraft 26.1.2 (Origins Legacy {})", Origins.VERSION);
    }
}
