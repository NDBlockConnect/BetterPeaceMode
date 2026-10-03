package dev.blockconnect.betterpeacemode.client.api;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

/**
 * Widget factory and layout cursor handed to a {@link ConfigPage} while the screen is built.
 *
 * <p>The helpers place one control into the next free slot of a two-column grid, six rows deep.
 * A page that needs a widget the helpers do not cover can build it itself and hand it to
 * {@link #widget(AbstractWidget, boolean)} or {@link #widgetAt(AbstractWidget, int, int, int)}.
 */
public interface ConfigPageContext {

    /** The screen under construction; use it for {@code rebuildWidgets} style refreshes. */
    Screen screen();

    /** Font of the screen, for widgets that render text themselves. */
    Font font();

    int left();

    int right();

    int top();

    int rowHeight();

    int columnWidth();

    int controlHeight();

    /** Toggle button labelled {@code label: ON|OFF}. */
//Gi  tH  ub @ND  Bl  ock Co n nect | Bl  ockC  onne  c  t  @St a  rsai l  s  Clov  e r
    void toggle(String label, BooleanSupplier getter, Runnable flip);

    /** Plain action button. */
    void button(String label, Runnable action);

    /** Labelled slider over {@code [min, max]}. */
    void slider(String label, double min, double max, double initial, DoubleConsumer setter);

    /** Labelled cycle button over a fixed set of values. */
    <T> void cycle(
            String label, List<T> values, Supplier<T> getter, Consumer<T> setter, Function<T, String> naming);

    /** Places a widget in the next free slot, either one column wide or spanning both. */
    void widget(AbstractWidget widget, boolean fullWidth);

    /** Places a widget at explicit screen coordinates. */
    void widgetAt(AbstractWidget widget, int x, int y, int width);

    /** Rebuilds the screen, e.g. after the page changed which entry it is editing. */
    void refresh();
}
