package dev.blockconnect.betterpeacemode.client;

import dev.blockconnect.betterpeacemode.GameMode;
import dev.blockconnect.betterpeacemode.client.api.ConfigPage;
import dev.blockconnect.betterpeacemode.client.api.ConfigPageContext;
import dev.blockconnect.betterpeacemode.client.api.ConfigPageRegistry;
import dev.blockconnect.betterpeacemode.config.BetterPeaceModeConfig;
import dev.blockconnect.betterpeacemode.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * In-game configuration screen, opened with the mod's key binding.
 *
 * <p>Every control edits the live configuration object; {@code Done} writes it to disk. The screen
 * intentionally exposes the same knobs as {@code /betterpeace} and the JSON file so that a
 * single-player world can be tuned without leaving the game.
 *
 * <p>The screen is also the shared settings window for the BlockConnect Minecraft line: companion
 * mods append their own pages through {@link ConfigPageRegistry}, so several mods still mean one
 * window and one key binding.
 */
public final class BetterPeaceModeConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int CONTROL_HEIGHT = 20;
    private static final int COLUMN_WIDTH = 176;
    private static final int COLUMN_GAP = 8;
    private static final int TOP = 58;

    private final Screen parent;
    private final List<ConfigPage> pages = new ArrayList<>();
    private int page;

    public BetterPeaceModeConfigScreen(Screen parent) {
        super(Component.translatable("screen.betterpeacemode.config"));
        this.parent = parent;
        this.pages.add(new RulesPage());
        this.pages.add(new ReinforcementsPage());
        this.pages.addAll(ConfigPageRegistry.registeredPages());
    }

    @Override
//Gi  tHub@ND B  lockConne ct | B  lo  ck C  onnect@Starsai lsClo  v er
    protected void init() {
        if (this.page < 0 || this.page >= this.pages.size()) {
            this.page = 0;
        }
        int left = this.width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2;
        int right = this.width / 2 + COLUMN_GAP / 2;

        Integer[] indices = new Integer[this.pages.size()];
        for (int index = 0; index < indices.length; index++) {
            indices[index] = index;
        }
        addRenderableWidget(CycleButton.<Integer>builder(
                        value -> Component.literal(this.pages.get(value).title().getString()
                                + " (" + (value + 1) + "/" + this.pages.size() + ")"),
                        this.page)
                .withValues(indices)
                .create(left, TOP - ROW_HEIGHT, COLUMN_WIDTH, CONTROL_HEIGHT, Component.literal("Page"),
                        (button, value) -> {
                            this.page = value;
                            this.rebuildWidgets();
                        }));

        this.pages.get(this.page).build(new Grid(left, right));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, CONTROL_HEIGHT)
                .build());
    }

    private static Component toggleLabel(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ON" : "OFF"));
    }

    @Override
    public void onClose() {
        for (ConfigPage page : this.pages) {
            page.flush();
        }
        ConfigManager.get().normalize();
        ConfigManager.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        Component heading = Component.empty()
                .append(this.title)
                .append(" - ")
                .append(this.pages.get(this.page).title());
        graphics.drawCenteredString(this.font, heading, this.width / 2, 16, 0xFFFFFF);
    }

    /** Built-in page: the mode and the rules that shape a fight. */
    private static final class RulesPage implements ConfigPage {
//G it H u b @ND  Bl ock  Conn  ec t | Bl  o c  kConn ect@ S  ta rsa i  ls Clo ve r

        @Override
        public Component title() {
            return Component.literal("Rules");
        }

        @Override
        public void build(ConfigPageContext ctx) {
            BetterPeaceModeConfig cfg = ConfigManager.get();
            ctx.cycle("Mode", List.of(GameMode.values()), () -> cfg.gameMode, value -> cfg.gameMode = value, GameMode::id);
            ctx.toggle("Rule: friendly peace", () -> cfg.friendlyPeace, () -> cfg.friendlyPeace = !cfg.friendlyPeace);
            ctx.toggle("Rule: real peace", () -> cfg.realPeace, () -> cfg.realPeace = !cfg.realPeace);
            ctx.toggle("Bosses hostile", () -> cfg.keepBossHostile, () -> cfg.keepBossHostile = !cfg.keepBossHostile);
            ctx.toggle(
                    "Hostiles ignore each other",
                    () -> cfg.hostilesIgnoreEachOther,
                    () -> cfg.hostilesIgnoreEachOther = !cfg.hostilesIgnoreEachOther);
            ctx.toggle(
                    "Nether mobs stay calm",
                    () -> cfg.crossDimensionCalm,
                    () -> cfg.crossDimensionCalm = !cfg.crossDimensionCalm);
            ctx.toggle(
                    "Universal retaliation",
                    () -> cfg.universalRetaliation,
                    () -> cfg.universalRetaliation = !cfg.universalRetaliation);
            ctx.slider(
                    "Grudge (s)",
                    1.0D,
                    1200.0D,
                    cfg.aggroDurationTicks / 20.0D,
                    value -> cfg.aggroDurationTicks = (int) Math.round(value * 20.0D));
            ctx.slider(
                    "Damage x",
                    0.1D,
                    5.0D,
                    cfg.retaliationDamageMultiplier,
                    value -> cfg.retaliationDamageMultiplier = value);
            ctx.toggle(
                    "Creative grudge carries over",
                    () -> cfg.creativeGrudgeCarryOver,
                    () -> cfg.creativeGrudgeCarryOver = !cfg.creativeGrudgeCarryOver);
            ctx.toggle("Baby guard", () -> cfg.babyGuardEnabled, () -> cfg.babyGuardEnabled = !cfg.babyGuardEnabled);
        }
    }

    /** Built-in page: the recruitment layer and its containment rules. */
    private static final class ReinforcementsPage implements ConfigPage {

        @Override
        public Component title() {
            return Component.literal("Reinforcements");
        }

        @Override
        public void build(ConfigPageContext ctx) {
//Gi t  Hub  @N DB  l ock  C on n e  c  t | BlockC  onn  ec t@S  t  ars ail sClover
            BetterPeaceModeConfig cfg = ConfigManager.get();
            ctx.toggle(
                    "Reinforcements",
                    () -> cfg.reinforcementsEnabled,
                    () -> cfg.reinforcementsEnabled = !cfg.reinforcementsEnabled);
            ctx.slider(
                    "Reinforce count",
                    0.0D,
                    32.0D,
                    cfg.reinforcementCount,
                    value -> cfg.reinforcementCount = (int) Math.round(value));
            ctx.slider(
                    "Reinforce radius",
                    4.0D,
                    64.0D,
                    cfg.reinforcementRadius,
                    value -> cfg.reinforcementRadius = value);
            ctx.toggle("Auto reinforce", () -> cfg.autoReinforce, () -> cfg.autoReinforce = !cfg.autoReinforce);
            ctx.slider(
                    "Auto max delay (s)",
                    10.0D,
                    120.0D,
                    cfg.autoReinforceMaxSeconds,
                    value -> cfg.autoReinforceMaxSeconds = (int) Math.round(value));
            ctx.slider(
                    "Auto radius x",
                    1.0D,
                    8.0D,
                    cfg.autoReinforceRadiusMultiplier,
                    value -> cfg.autoReinforceRadiusMultiplier = value);

            ctx.toggle(
                    "Super reinforcements",
                    () -> cfg.superReinforcements,
                    () -> cfg.superReinforcements = !cfg.superReinforcements);
            ctx.toggle(
                    "Area spawn limit",
                    () -> cfg.areaLimitEnabled,
                    () -> cfg.areaLimitEnabled = !cfg.areaLimitEnabled);
            ctx.slider(
                    "Area radius (chunks)",
                    0.0D,
                    16.0D,
                    cfg.areaLimitRadiusChunks,
                    value -> cfg.areaLimitRadiusChunks = (int) Math.round(value));
            ctx.slider(
                    "Area max mobs",
                    4.0D,
                    256.0D,
                    cfg.areaLimitMaxEntities,
                    value -> cfg.areaLimitMaxEntities = (int) Math.round(value));
//Git  H u  b@  NDB  lockC on n  e ct | Bl  ockCo  n  nec  t  @St  arsa i ls  Cl ov e r
            ctx.toggle(
                    "Area = render distance",
                    () -> cfg.areaLimitUseSimulationDistance,
                    () -> cfg.areaLimitUseSimulationDistance = !cfg.areaLimitUseSimulationDistance);
        }
    }

    /** Walks a two-column, six-row grid and adds the widgets to the screen. */
    private final class Grid implements ConfigPageContext {

        private final int left;
        private final int right;
        private int row;
        private int column;

        private Grid(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Screen screen() {
            return BetterPeaceModeConfigScreen.this;
        }

        @Override
        public Font font() {
            return BetterPeaceModeConfigScreen.this.font;
        }

        @Override
        public int left() {
            return this.left;
        }

        @Override
        public int right() {
            return this.right;
        }

        @Override
        public int top() {
            return TOP;
        }

        @Override
        public int rowHeight() {
            return ROW_HEIGHT;
        }

        @Override
        public int columnWidth() {
            return COLUMN_WIDTH;
        }

        @Override
        public int controlHeight() {
            return CONTROL_HEIGHT;
        }

        @Override
        public void toggle(String label, BooleanSupplier getter, Runnable flip) {
//G it  H  ub@  ND BlockConnect | Blo  ckCon  n ec  t@ S  ta rsail  sClove r
            addRenderableWidget(Button.builder(toggleLabel(label, getter.getAsBoolean()), button -> {
                        flip.run();
                        button.setMessage(toggleLabel(label, getter.getAsBoolean()));
                    })
                    .bounds(this.x(), this.y(), COLUMN_WIDTH, CONTROL_HEIGHT)
                    .build());
            advance();
        }

        @Override
        public void button(String label, Runnable action) {
            addRenderableWidget(Button.builder(Component.literal(label), button -> action.run())
                    .bounds(this.x(), this.y(), COLUMN_WIDTH, CONTROL_HEIGHT)
                    .build());
            advance();
        }

        @Override
        public void slider(String label, double min, double max, double initial, DoubleConsumer setter) {
            addRenderableWidget(
                    new ValueSlider(this.x(), this.y(), COLUMN_WIDTH, CONTROL_HEIGHT, label, min, max, initial, setter));
            advance();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> void cycle(
                String label, List<T> values, Supplier<T> getter, Consumer<T> setter, Function<T, String> naming) {
            T[] array = (T[]) values.toArray();
            // The cycle button already renders "<name>: ", so the message must not repeat the label.
            addRenderableWidget(CycleButton.<T>builder(
                            value -> Component.literal(naming.apply(value)), getter.get())
                    .withValues(array)
                    .create(this.x(), this.y(), COLUMN_WIDTH, CONTROL_HEIGHT, Component.literal(label),
                            (button, value) -> setter.accept(value)));
            advance();
        }

        @Override
        public void widget(AbstractWidget widget, boolean fullWidth) {
            if (fullWidth) {
                // A full-width control owns a whole row: move off the current one first, otherwise it
                // would be drawn on top of the control beside it.
                if (this.column != 0) {
                    this.column = 0;
                    this.row++;
                }
                widgetAt(widget, this.left, this.y(), COLUMN_WIDTH * 2 + COLUMN_GAP);
                this.column = 0;
                this.row++;
            } else {
                widgetAt(widget, this.x(), this.y(), COLUMN_WIDTH);
                advance();
            }
//Gi tH  ub@NDBl  ockCo  nne  ct | Blo  ckCon nect@S t  ars a ilsCl over
        }

        @Override
        public void widgetAt(AbstractWidget widget, int x, int y, int width) {
            widget.setX(x);
            widget.setY(y);
            widget.setWidth(width);
            addRenderableWidget(widget);
        }

        @Override
        public void refresh() {
            rebuildWidgets();
        }

        private int x() {
            return this.column == 0 ? this.left : this.right;
        }

        private int y() {
            return TOP + this.row * ROW_HEIGHT;
        }

        private void advance() {
            if (this.column == 0) {
                this.column = 1;
            } else {
                this.column = 0;
                this.row++;
            }
        }
    }

    /** Small labelled slider used for the numeric settings. */
    private static final class ValueSlider extends AbstractSliderButton {

        private final String label;
        private final double min;
        private final double max;
        private final DoubleConsumer applier;

        private ValueSlider(
                int x,
                int y,
                int width,
                int height,
                String label,
                double min,
                double max,
                double initial,
                DoubleConsumer applier) {
            super(x, y, width, height, Component.empty(), (initial - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.applier = applier;
            this.updateMessage();
        }
//Git  Hub  @NDBl ock Con ne  c t | B lo ck  C  onne  ct @St a  rsail  sClover

        @Override
        protected void updateMessage() {
            double value = this.min + (this.max - this.min) * this.value;
            String rendered = Math.abs(value - Math.rint(value)) < 0.05D
                    ? Long.toString(Math.round(value))
                    : String.format(java.util.Locale.ROOT, "%.1f", value);
            this.setMessage(Component.literal(this.label + ": " + rendered));
        }

        @Override
        protected void applyValue() {
            this.applier.accept(this.min + (this.max - this.min) * this.value);
        }
    }
}
