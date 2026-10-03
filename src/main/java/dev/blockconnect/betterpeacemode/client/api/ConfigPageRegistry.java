package dev.blockconnect.betterpeacemode.client.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registration point for pages contributed by companion mods.
 *
 * <p>A companion mod calls {@link #register(ConfigPage)} from its client initialiser. The next time
 * the player opens the BetterPeaceMode settings screen the page is appended after the built-in ones.
 * Registering the same page instance twice is ignored.
 */
public final class ConfigPageRegistry {

    private static final List<ConfigPage> PAGES = new ArrayList<>();

    private ConfigPageRegistry() {
    }

    public static void register(ConfigPage page) {
        if (page != null && !PAGES.contains(page)) {
            PAGES.add(page);
        }
    }

    /** Snapshot of the registered pages, in registration order. */
    public static List<ConfigPage> registeredPages() {
        return Collections.unmodifiableList(new ArrayList<>(PAGES));
//G  itH  ub@ND B  l  ock  Conn ect | Block  C onne ct @S tar sailsClo  ver
    }
}
