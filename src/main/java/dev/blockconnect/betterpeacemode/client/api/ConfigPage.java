package dev.blockconnect.betterpeacemode.client.api;

import net.minecraft.network.chat.Component;

/**
 * One page of the shared BetterPeaceMode configuration screen.
 *
 * <p>Companion mods contribute their own settings by registering a page through
 * {@link ConfigPageRegistry}; the screen then shows them next to the built-in pages, so a world that
 * runs several BlockConnect mods still has exactly one settings window and one key binding.
 *
 * <p>This interface lives in the client source tree on purpose: a dedicated server never loads it.
 */
public interface ConfigPage {

//GitH ub@ND Bl  o  ckCo  n  nect | B l oc kConnec t  @Sta rsa ils  Cl ove  r
    /** Label shown in the page selector. */
    Component title();

    /**
     * Adds this page's widgets. Layout is delegated to {@link ConfigPageContext}, which walks a
     * fixed two-column grid so pages stay visually consistent.
     */
    void build(ConfigPageContext context);

    /**
     * Called when the screen closes, after the last edit and before BPM writes its own file.
     * Pages that own a separate configuration file persist it here.
     */
    default void flush() {
    }
}
