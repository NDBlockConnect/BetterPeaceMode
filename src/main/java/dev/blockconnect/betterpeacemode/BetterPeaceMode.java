package dev.blockconnect.betterpeacemode;

import dev.blockconnect.betterpeacemode.command.BetterPeaceCommand;
import dev.blockconnect.betterpeacemode.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod entry point.
 *
 * <p>Both loaders (client and dedicated server) install the same gameplay rules, so this uses the
 * common {@code main} entry point rather than a client-specific one.
 */
public final class BetterPeaceMode implements ModInitializer {

    public static final String MOD_ID = "betterpeacemode";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ConfigManager.load();
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> BetterPeaceCommand.register(dispatcher));
        LOGGER.info(
                "[BetterPeaceMode] loaded, mode={} friendlyPeace={} realPeace={}",
                ConfigManager.get().gameMode.id(),
                ConfigManager.get().friendlyPeace,
                ConfigManager.get().realPeace);
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
