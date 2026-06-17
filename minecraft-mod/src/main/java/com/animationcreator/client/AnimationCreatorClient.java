package com.animationcreator.client;

import com.animationcreator.client.gui.AnimationCreatorScreen;
import com.animationcreator.network.NetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AnimationCreatorClient implements ClientModInitializer {

    public static KeyBinding OPEN_CREATOR;
    public static KeyBinding OPEN_LIBRARY;

    @Override
    public void onInitializeClient() {
        // Register client-side network receivers
        NetworkHandler.registerClient();

        // Key bindings
        OPEN_CREATOR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.animationcreator.open_creator",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.animationcreator.main"
        ));
        OPEN_LIBRARY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.animationcreator.open_library",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.animationcreator.main"
        ));

        // Tick handler to detect key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CREATOR.wasPressed()) {
                client.setScreen(new AnimationCreatorScreen(null));
            }
            while (OPEN_LIBRARY.wasPressed()) {
                client.setScreen(new com.animationcreator.client.gui.AnimationLibraryScreen(null));
            }
        });
    }
}
