package dev.blockconnect.betterpeacemode.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Client entry point: opens the configuration screen.
 *
 * <p>The gameplay rules themselves are server-side, so this class only provides the operator
 * interface. In single-player the client and the integrated server share one configuration object,
 * which means an edit here takes effect immediately; on a dedicated server the operator uses
 * {@code /betterpeace} instead.
 */
public final class BetterPeaceModeClient implements ClientModInitializer {

    private static final String KEY_CONFIG = "key.betterpeacemode.config";

    private static KeyMapping openConfigKey;

    @Override
    public void onInitializeClient() {
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_CONFIG,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_B,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("betterpeacemode", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.screen == null) {
                    minecraft.setScreen(new BetterPeaceModeConfigScreen(null));
                }
            }
        });
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
