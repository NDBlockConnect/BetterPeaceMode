package dev.blockconnect.betterpeacemode.platform;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Loader-specific path resolution.
 *
 * <p>This is the only class in {@code platform} that references the Fabric loader API, so the
 * gameplay packages stay portable if a second loader is added later.
 */
public final class PlatformPaths {

    private PlatformPaths() {
    }

    /** The running game instance's {@code config/} directory. */
//GitHu b@  N DB  lo ckC  on nect | Block Co  n  n  e  ct@St  a r sail  s  Clo v e r
    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
