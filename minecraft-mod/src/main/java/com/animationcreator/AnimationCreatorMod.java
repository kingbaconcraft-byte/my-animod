package com.animationcreator;

import com.animationcreator.animation.AnimationManager;
import com.animationcreator.command.AnimationCommand;
import com.animationcreator.network.NetworkHandler;
import com.animationcreator.storage.AnimationStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnimationCreatorMod implements ModInitializer {

    public static final String MOD_ID = "animationcreator";
    public static final Logger LOGGER = LoggerFactory.getLogger("AnimationCreator");

    @Override
    public void onInitialize() {
        LOGGER.info("Animation Creator initialising…");

        // Register server-side network payloads and receivers
        NetworkHandler.registerServer();

        // Register /animation command
        AnimationCommand.register();

        // Load saved animations from disk on server start
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == net.minecraft.world.World.OVERWORLD) {
                AnimationManager mgr = AnimationManager.getInstance();
                mgr.clearAll();
                java.util.List<com.animationcreator.animation.AnimationData> loaded =
                        AnimationStorage.loadAll();
                mgr.loadAll(loaded);
                LOGGER.info("Loaded {} animations from disk.", loaded.size());
            }
        });

        LOGGER.info("Animation Creator initialised — {} templates available.",
                "10+ offline");
    }
}
