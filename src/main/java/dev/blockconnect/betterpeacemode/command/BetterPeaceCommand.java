package dev.blockconnect.betterpeacemode.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.blockconnect.betterpeacemode.GameMode;
import dev.blockconnect.betterpeacemode.config.BetterPeaceModeConfig;
import dev.blockconnect.betterpeacemode.config.ConfigManager;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Operator-facing control surface: {@code /betterpeace ...}.
 *
 * <p>Requires permission level 2 so that a dedicated-server owner keeps control of the mode while
 * ordinary players cannot flip world rules underneath everyone else.
 */
public final class BetterPeaceCommand {

    private BetterPeaceCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("betterpeace")
                // 1.21.11 replaced the integer permission level with a PermissionSet/PermissionCheck
                // model; LEVEL_GAMEMASTERS is the equivalent of the old level 2 gate.
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(BetterPeaceCommand::status)
                .then(Commands.literal("status").executes(BetterPeaceCommand::status))
                .then(Commands.literal("reload").executes(BetterPeaceCommand::reload))
                .then(Commands.literal("save").executes(BetterPeaceCommand::save))
                .then(Commands.literal("mode")
                        .executes(BetterPeaceCommand::status)
                        .then(Commands.argument("value", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (GameMode mode : GameMode.values()) {
                                        builder.suggest(mode.id());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(BetterPeaceCommand::setMode))));
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        BetterPeaceModeConfig cfg = ConfigManager.get();
        context.getSource().sendSuccess(() -> Component.literal(String.format(
                        Locale.ROOT,
                        "[BetterPeaceMode] mode=%s friendlyPeace=%s realPeace=%s difficulty=%s keepBossHostile=%s aggroTicks=%d",
                        cfg.gameMode.id(),
                        cfg.friendlyPeace,
                        cfg.realPeace,
                        dev.blockconnect.betterpeacemode.core.PeacePolicy.targetDifficulty(),
                        cfg.keepBossHostile,
                        cfg.aggroDurationTicks)),
                false);
        return 1;
    }

    private static int setMode(CommandContext<CommandSourceStack> context) {
        String raw = StringArgumentType.getString(context, "value");
        GameMode mode = GameMode.byId(raw);
        BetterPeaceModeConfig cfg = ConfigManager.get();
        cfg.gameMode = mode;
        cfg.friendlyPeace = false;
        cfg.realPeace = false;
        cfg.normalize();
        ConfigManager.save();
        context.getSource().sendSuccess(
                () -> Component.literal("[BetterPeaceMode] mode set to " + mode.id()), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        ConfigManager.load();
        context.getSource().sendSuccess(
                () -> Component.literal("[BetterPeaceMode] configuration reloaded from disk"), true);
        return status(context);
    }

    private static int save(CommandContext<CommandSourceStack> context) {
        ConfigManager.save();
        context.getSource().sendSuccess(
                () -> Component.literal("[BetterPeaceMode] configuration written to disk"), true);
        return 1;
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
