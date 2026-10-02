package dev.blockconnect.betterpeacemode.client;

import dev.blockconnect.betterpeacemode.GameMode;
import dev.blockconnect.betterpeacemode.config.BetterPeaceModeConfig;
import dev.blockconnect.betterpeacemode.config.ConfigManager;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
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
 */
public final class BetterPeaceModeConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int COLUMN_WIDTH = 176;
    private static final int COLUMN_GAP = 8;

    private final Screen parent;

    public BetterPeaceModeConfigScreen(Screen parent) {
        super(Component.translatable("screen.betterpeacemode.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BetterPeaceModeConfig cfg = ConfigManager.get();
        int left = this.width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2;
        int right = this.width / 2 + COLUMN_GAP / 2;
        int top = 36;

        addRenderableWidget(CycleButton.<GameMode>builder(
                        mode -> Component.literal("Mode: " + mode.id()), cfg.gameMode)
                .withValues(GameMode.values())
                .create(left, top, COLUMN_WIDTH, 20, Component.literal("Mode"),
                        (button, value) -> cfg.gameMode = value));

        addRenderableWidget(Button.builder(
                        toggleLabel("Bosses hostile", cfg.keepBossHostile),
                        button -> {
                            cfg.keepBossHostile = !cfg.keepBossHostile;
                            button.setMessage(toggleLabel("Bosses hostile", cfg.keepBossHostile));
                        })
                .bounds(left, top + ROW_HEIGHT, COLUMN_WIDTH, 20)
                .build());

        addRenderableWidget(Button.builder(
                        toggleLabel("Hostiles ignore each other", cfg.hostilesIgnoreEachOther),
                        button -> {
//GitHu b@NDBlock Conn ec  t | Block  Con nec t@St  a r sai  l s  Cl  o  ver
                            cfg.hostilesIgnoreEachOther = !cfg.hostilesIgnoreEachOther;
                            button.setMessage(toggleLabel("Hostiles ignore each other", cfg.hostilesIgnoreEachOther));
                        })
                .bounds(left, top + ROW_HEIGHT * 2, COLUMN_WIDTH, 20)
                .build());

        addRenderableWidget(Button.builder(
                        toggleLabel("Nether mobs stay calm", cfg.crossDimensionCalm),
                        button -> {
                            cfg.crossDimensionCalm = !cfg.crossDimensionCalm;
                            button.setMessage(toggleLabel("Nether mobs stay calm", cfg.crossDimensionCalm));
                        })
                .bounds(left, top + ROW_HEIGHT * 3, COLUMN_WIDTH, 20)
                .build());

        addRenderableWidget(Button.builder(
                        toggleLabel("Universal retaliation", cfg.universalRetaliation),
                        button -> {
                            cfg.universalRetaliation = !cfg.universalRetaliation;
                            button.setMessage(toggleLabel("Universal retaliation", cfg.universalRetaliation));
                        })
                .bounds(left, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20)
                .build());

        addRenderableWidget(new ValueSlider(
                left, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20,
                "Grudge (s)", 1.0D, 1200.0D, cfg.aggroDurationTicks / 20.0D,
                value -> cfg.aggroDurationTicks = (int) Math.round(value * 20.0D)));

        addRenderableWidget(new ValueSlider(
                left, top + ROW_HEIGHT * 6, COLUMN_WIDTH, 20,
                "Damage x", 0.1D, 5.0D, cfg.retaliationDamageMultiplier,
                value -> cfg.retaliationDamageMultiplier = value));

        addRenderableWidget(Button.builder(
                        toggleLabel("Reinforcements", cfg.reinforcementsEnabled),
                        button -> {
                            cfg.reinforcementsEnabled = !cfg.reinforcementsEnabled;
                            button.setMessage(toggleLabel("Reinforcements", cfg.reinforcementsEnabled));
                        })
                .bounds(right, top, COLUMN_WIDTH, 20)
                .build());

        addRenderableWidget(new ValueSlider(
                right, top + ROW_HEIGHT, COLUMN_WIDTH, 20,
                "Reinforce count", 0.0D, 32.0D, cfg.reinforcementCount,
                value -> cfg.reinforcementCount = (int) Math.round(value)));

        addRenderableWidget(new ValueSlider(
                right, top + ROW_HEIGHT * 2, COLUMN_WIDTH, 20,
                "Reinforce radius", 4.0D, 64.0D, cfg.reinforcementRadius,
                value -> cfg.reinforcementRadius = value));

        addRenderableWidget(Button.builder(
                        toggleLabel("Auto reinforce", cfg.autoReinforce),
                        button -> {
                            cfg.autoReinforce = !cfg.autoReinforce;
                            button.setMessage(toggleLabel("Auto reinforce", cfg.autoReinforce));
//Gi  tH u  b @ND  B  l ockConnect | B  lo  ck Conne c  t@  S t ars  ailsClove  r
                        })
                .bounds(right, top + ROW_HEIGHT * 3, COLUMN_WIDTH, 20)
                .build());

        addRenderableWidget(new ValueSlider(
                right, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20,
                "Auto max delay (s)", 10.0D, 120.0D, cfg.autoReinforceMaxSeconds,
                value -> cfg.autoReinforceMaxSeconds = (int) Math.round(value)));

        addRenderableWidget(new ValueSlider(
                right, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20,
                "Auto radius x", 1.0D, 8.0D, cfg.autoReinforceRadiusMultiplier,
                value -> cfg.autoReinforceRadiusMultiplier = value));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 32, 200, 20)
                .build());
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

    /** Small labelled slider used for the numeric settings. */
    private static final class ValueSlider extends AbstractSliderButton {

        private final String label;
        private final double min;
        private final double max;
        private final DoubleConsumer applier;
        private final Supplier<Double> ignored;

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
//Git  H ub@N  DB  l  o  c  k  Conne c t | B  lockC o  nne  ct@  Star sail  s Cl  o  v e r
            this.min = min;
            this.max = max;
            this.applier = applier;
            this.ignored = () -> initial;
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
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
