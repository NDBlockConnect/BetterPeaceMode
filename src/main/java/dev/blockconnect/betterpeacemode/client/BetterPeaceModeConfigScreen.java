package dev.blockconnect.betterpeacemode.client;

import dev.blockconnect.betterpeacemode.GameMode;
import dev.blockconnect.betterpeacemode.config.BetterPeaceModeConfig;
import dev.blockconnect.betterpeacemode.config.ConfigManager;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
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
 * <p>The settings outgrew one screen when the reinforcement protections arrived, so they are split
 * over two pages: the rules themselves, and the recruitment layer. Each page keeps two columns so
 * the layout still fits a default-size window.
 */
public final class BetterPeaceModeConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int CONTROL_HEIGHT = 20;
    private static final int COLUMN_WIDTH = 176;
    private static final int COLUMN_GAP = 8;
    private static final int TOP = 58;
    private static final int RULES_PAGE = 0;
    private static final int REINFORCEMENTS_PAGE = 1;

    private final Screen parent;
    private int page = RULES_PAGE;

    public BetterPeaceModeConfigScreen(Screen parent) {
        super(Component.translatable("screen.betterpeacemode.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BetterPeaceModeConfig cfg = ConfigManager.get();
        int left = this.width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2;
        int right = this.width / 2 + COLUMN_GAP / 2;

        addRenderableWidget(CycleButton.<Integer>builder(
                        value -> Component.literal(
                                "Page: " + (value == RULES_PAGE ? "Rules" : "Reinforcements")),
                        this.page)
                .withValues(RULES_PAGE, REINFORCEMENTS_PAGE)
                .create(left, TOP - ROW_HEIGHT, COLUMN_WIDTH, CONTROL_HEIGHT, Component.literal("Page"),
                        (button, value) -> {
                            this.page = value;
//G  i  tHub  @  N D Bl ockC onn  ect | Blo ckConne  ct @St  arsailsC love r
                            this.rebuildWidgets();
                        }));

        if (this.page == RULES_PAGE) {
            this.buildRulesPage(cfg, left, right);
        } else {
            this.buildReinforcementsPage(cfg, left, right);
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, CONTROL_HEIGHT)
                .build());
    }

    private void buildRulesPage(BetterPeaceModeConfig cfg, int left, int right) {
        addRenderableWidget(CycleButton.<GameMode>builder(
                        mode -> Component.literal("Mode: " + mode.id()), cfg.gameMode)
                .withValues(GameMode.values())
                .create(left, TOP, COLUMN_WIDTH, CONTROL_HEIGHT, Component.literal("Mode"),
                        (button, value) -> cfg.gameMode = value));

        addRenderableWidget(toggle(left, TOP + ROW_HEIGHT, "Bosses hostile",
                () -> cfg.keepBossHostile, () -> cfg.keepBossHostile = !cfg.keepBossHostile));
        addRenderableWidget(toggle(left, TOP + ROW_HEIGHT * 2, "Hostiles ignore each other",
                () -> cfg.hostilesIgnoreEachOther,
                () -> cfg.hostilesIgnoreEachOther = !cfg.hostilesIgnoreEachOther));
        addRenderableWidget(toggle(left, TOP + ROW_HEIGHT * 3, "Nether mobs stay calm",
                () -> cfg.crossDimensionCalm, () -> cfg.crossDimensionCalm = !cfg.crossDimensionCalm));

        addRenderableWidget(toggle(right, TOP, "Universal retaliation",
                () -> cfg.universalRetaliation,
                () -> cfg.universalRetaliation = !cfg.universalRetaliation));
        addRenderableWidget(new ValueSlider(
                right, TOP + ROW_HEIGHT, COLUMN_WIDTH, CONTROL_HEIGHT,
                "Grudge (s)", 1.0D, 1200.0D, cfg.aggroDurationTicks / 20.0D,
                value -> cfg.aggroDurationTicks = (int) Math.round(value * 20.0D)));
        addRenderableWidget(new ValueSlider(
                right, TOP + ROW_HEIGHT * 2, COLUMN_WIDTH, CONTROL_HEIGHT,
                "Damage x", 0.1D, 5.0D, cfg.retaliationDamageMultiplier,
                value -> cfg.retaliationDamageMultiplier = value));
        addRenderableWidget(toggle(right, TOP + ROW_HEIGHT * 3, "Baby guard",
                () -> cfg.babyGuardEnabled, () -> cfg.babyGuardEnabled = !cfg.babyGuardEnabled));
    }

    private void buildReinforcementsPage(BetterPeaceModeConfig cfg, int left, int right) {
        addRenderableWidget(toggle(left, TOP, "Reinforcements",
                () -> cfg.reinforcementsEnabled,
                () -> cfg.reinforcementsEnabled = !cfg.reinforcementsEnabled));
        addRenderableWidget(new ValueSlider(
                left, TOP + ROW_HEIGHT, COLUMN_WIDTH, CONTROL_HEIGHT,
                "Reinforce count", 0.0D, 32.0D, cfg.reinforcementCount,
                value -> cfg.reinforcementCount = (int) Math.round(value)));
        addRenderableWidget(new ValueSlider(
                left, TOP + ROW_HEIGHT * 2, COLUMN_WIDTH, CONTROL_HEIGHT,
                "Reinforce radius", 4.0D, 64.0D, cfg.reinforcementRadius,
                value -> cfg.reinforcementRadius = value));
//Gi  t Hub@N DB  l o  ckCon nect | Block Conne  ct @S  tars  ail  sCl ove r
        addRenderableWidget(toggle(left, TOP + ROW_HEIGHT * 3, "Auto reinforce",
                () -> cfg.autoReinforce, () -> cfg.autoReinforce = !cfg.autoReinforce));
        addRenderableWidget(new ValueSlider(
                left, TOP + ROW_HEIGHT * 4, COLUMN_WIDTH, CONTROL_HEIGHT,
                "Auto max delay (s)", 10.0D, 120.0D, cfg.autoReinforceMaxSeconds,
                value -> cfg.autoReinforceMaxSeconds = (int) Math.round(value)));
        addRenderableWidget(new ValueSlider(
                left, TOP + ROW_HEIGHT * 5, COLUMN_WIDTH, CONTROL_HEIGHT,
                "Auto radius x", 1.0D, 8.0D, cfg.autoReinforceRadiusMultiplier,
                value -> cfg.autoReinforceRadiusMultiplier = value));

        addRenderableWidget(toggle(right, TOP, "Super reinforcements",
                () -> cfg.superReinforcements, () -> cfg.superReinforcements = !cfg.superReinforcements));
        addRenderableWidget(toggle(right, TOP + ROW_HEIGHT, "Area spawn limit",
                () -> cfg.areaLimitEnabled, () -> cfg.areaLimitEnabled = !cfg.areaLimitEnabled));
        addRenderableWidget(new ValueSlider(
                right, TOP + ROW_HEIGHT * 2, COLUMN_WIDTH, CONTROL_HEIGHT,
                "Area radius (chunks)", 0.0D, 16.0D, cfg.areaLimitRadiusChunks,
                value -> cfg.areaLimitRadiusChunks = (int) Math.round(value)));
        addRenderableWidget(new ValueSlider(
                right, TOP + ROW_HEIGHT * 3, COLUMN_WIDTH, CONTROL_HEIGHT,
                "Area max mobs", 4.0D, 256.0D, cfg.areaLimitMaxEntities,
                value -> cfg.areaLimitMaxEntities = (int) Math.round(value)));
        addRenderableWidget(toggle(right, TOP + ROW_HEIGHT * 4, "Area = render distance",
                () -> cfg.areaLimitUseSimulationDistance,
                () -> cfg.areaLimitUseSimulationDistance = !cfg.areaLimitUseSimulationDistance));
    }

    private static Button toggle(int x, int y, String label, BooleanSupplier getter, Runnable flip) {
        return Button.builder(toggleLabel(label, getter.getAsBoolean()), button -> {
                    flip.run();
                    button.setMessage(toggleLabel(label, getter.getAsBoolean()));
                })
                .bounds(x, y, COLUMN_WIDTH, CONTROL_HEIGHT)
                .build();
    }

    private static Component toggleLabel(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ON" : "OFF"));
    }

    @Override
    public void onClose() {
        ConfigManager.get().normalize();
        ConfigManager.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
    }
//GitHu  b@ N  DBlock  Connec  t | Blo ckCo  n  n  e  ct@S tars ail s  Clove r

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
