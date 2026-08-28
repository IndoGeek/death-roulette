package com.indogeek.deathroulette.command;

import com.indogeek.deathroulette.game.DeathRouletteGame;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class DeathRouletteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("roulette")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("start")
                                .executes(context -> {
                                    DeathRouletteGame game = DeathRouletteGame.getInstance();
                                    if (game.isRunning()) {
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("§cDeath Roulette is already running."),
                                                false
                                        );
                                        return 0;
                                    }
                                    game.start(context.getSource().getServer());
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("§aDeath Roulette started!"),
                                            true
                                    );
                                    return 1;
                                }))
                        .then(CommandManager.literal("stop")
                                .executes(context -> {
                                    DeathRouletteGame game = DeathRouletteGame.getInstance();
                                    if (!game.isRunning()) {
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("§cDeath Roulette is not running."),
                                                false
                                        );
                                        return 0;
                                    }
                                    game.stop();
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("§eDeath Roulette stopped."),
                                            true
                                    );
                                    return 1;
                                }))
                        .then(CommandManager.literal("status")
                            .executes(context -> {
                                DeathRouletteGame game = DeathRouletteGame.getInstance();
                                if (game.isRunning()) {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal(
                                            "§6Death Roulette: §aRUNNING §7| Day: §e"
                                            + game.getElapsedDays(context.getSource().getServer())
                                            + " §7| Ticks: §e"
                                            + game.getTicksIntoCurrentDay(context.getSource().getServer())
                                        ),
                                        false
                                    );
                                } else {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("§6Death Roulette: §cSTOPPED"),
                                        false
                                    );
                                }
                                return 1;
                             }))
                        .then(CommandManager.literal("testday")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(context -> {
                                DeathRouletteGame game = DeathRouletteGame.getInstance();
                                if (!game.isRunning()) {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("$cDeath Roulette is not running."),
                                        false
                                    );
                                    return 0;
                                }
                                game.processTestDay(context.getSource().getServer());
                                context.getSource().sendFeedback(
                                    () -> Text.literal("$aTest day processed."),
                                    true
                                );
                                return 1;
                            })
                        )
        );
    }
}
