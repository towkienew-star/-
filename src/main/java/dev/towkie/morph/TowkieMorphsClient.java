package dev.towkie.morph;

import dev.towkie.morph.gui.MorphScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class TowkieMorphsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MorphStore.load();
        KeyBinding openKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.towkiemorphs.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "category.towkiemorphs")
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MorphStore.syncOnlinePlayers(client);
            while (openKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new MorphScreen());
                }
            }
        });
    }
}
