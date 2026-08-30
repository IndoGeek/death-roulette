package com.indogeek.deathroulette.command;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;
import com.indogeek.deathroulette.game.DeathRouletteGame;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import com.indogeek.deathroulette.DeathRoulette;

public class DeathRouletteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("roulette")
                        .requires(source -> DeathRoulette.CONFIG.canUseCommands(source))
                        .then(CommandManager.literal("start")
                                .executes(context -> {
                                    DeathRouletteGame game = DeathRouletteGame.getInstance();
                                    if (game.isRunning()) {
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("$cDeath Roulette is already running."),
                                                false
                                        );
                                        return 0;
                                    }
                                    game.start(context.getSource().getServer());
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("$aDeath Roulette started!"),
                                            true
                                    );
                                    return 1;
                                }))
                        .then(CommandManager.literal("stop")
                                .executes(context -> {
                                    DeathRouletteGame game = DeathRouletteGame.getInstance();
                                    if (!game.isRunning()) {
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("$cDeath Roulette is not running."),
                                                false
                                        );
                                        return 0;
                                    }
                                    game.stop(context.getSource().getServer());
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("$eDeath Roulette stopped."),
                                            true
                                    );
                                    return 1;
                                }))
                        .then(CommandManager.literal("status")
                            .executes(context -> {
                                DeathRouletteGame game = DeathRouletteGame.getInstance();
                                if (game.isRunning()) {
                                    List<ServerPlayerEntity> players =
                                        context.getSource().getServer().getPlayerManager().getPlayerList();
                                    StringBuilder playerNames = new StringBuilder();
                                    for (ServerPlayerEntity player : players) {
                                        if (playerNames.length() > 0) {
                                            playerNames.append(", ");
                                        }
                                        playerNames.append(player.getName().getString());
                                    }
                                    context.getSource().sendFeedback(
                                        () -> Text.literal(
                                            "$6Death Roulette: $aRUNNING $7| Day: $e"
                                            + game.getElapsedDays(context.getSource().getServer())
                                            + " $7| Ticks: $e"
                                            + game.getTicksIntoCurrentDay(context.getSource().getServer())
                                            + " $7| Players: $e"
                                            + game.getOnlinePlayerCount(context.getSource().getServer())
                                            + " $7| Online: $e"
                                            + playerNames
                                        ),
                                        false
                                    );
                                } else {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("$6Death Roulette: $cSTOPPED"),
                                        false
                                    );
                                }
                                return 1;
                            })
                        )
                        .then(CommandManager.literal("reload")
                            .executes(context -> {
                                MinecraftServer server = context.getSource().getServer();
                                DeathRouletteGame game = DeathRouletteGame.getInstance();
                                DeathRoulette.CONFIG.load();
                                game.showActionBar(server, "§aDeath Roulette configuration reloaded!");
                                DeathRoulette.LOGGER.info("Configuration reloaded.");
                                return 1;
                            })
                        )
                        .then(CommandManager.literal("test")
                            .executes(context -> {
                                DeathRouletteGame game = DeathRouletteGame.getInstance();
                                MinecraftServer server = context.getSource().getServer();
                                game.startTest(server);
                                game.startCountdown(server);
                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§aRoulette test started. Forced to Day 10."
                                    ),
                                    true
                                );
                                return 1;
                            })
                        )
        );
    }
}
