package com.indogeek.deathroulette.command;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;
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
                                    game.stop();
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
                        .then(CommandManager.literal("testrandom")
                            .executes(context -> {
                                DeathRouletteGame game = DeathRouletteGame.getInstance();
                                if (!game.isRunning()) {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("$cDeath Roulette is not running."),
                                        false
                                    );
                                    return 0;
                                }
                                ServerPlayerEntity player =
                                    game.getRandomPlayer(context.getSource().getServer());
                                if (player == null) {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("$cNo online players found."),
                                        false
                                    );
                                    return 0;
                                }
                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "$6Random player selected: $e" + player.getName().getString()
                                    ),
                                    false
                                );
                                return 1;
                            })
                        )
                        .then(CommandManager.literal("testannounce")
                        .requires(source -> source.hasPermissionLevel(4))
                        .executes(context -> {
                            DeathRouletteGame game = DeathRouletteGame.getInstance();
                            if (!game.isRunning()) {
                                context.getSource().sendFeedback(
                                    () -> Text.literal("§cDeath Roulette is not running."),
                                    false
                                );
                                return 0;
                            }
                            game.announce(
                                context.getSource().getServer(),
                                "§6Death Roulette: §eAnnouncement test successful!"
                            );
                            context.getSource().sendFeedback(
                                () -> Text.literal("§aAnnouncement sent to all online players."),
                                false
                            );
                            return 1;
                        }))
                        .then(CommandManager.literal("testcountdown")
                            .requires(source -> source.hasPermissionLevel(4))
                            .executes(context -> {
                                DeathRouletteGame game = DeathRouletteGame.getInstance();
                                if (!game.isRunning()) {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("§cDeath Roulette is not running."),
                                        false
                                    );
                                    return 0;
                                }
                                game.startCountdown(context.getSource().getServer());
                                context.getSource().sendFeedback(
                                    () -> Text.literal("§aCountdown started."),
                                    false
                                );
                                return 1;
                            })
                        )
                        .then(CommandManager.literal("testchance")
                            .requires(source -> source.hasPermissionLevel(4))
                            .executes(context -> {
                                DeathRouletteGame game = DeathRouletteGame.getInstance();
                                if (!game.isRunning()) {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("§cDeath Roulette is not running."),
                                        false
                                    );
                                    return 0;
                                }
                                boolean playerResult = game.isPlayerResult();
                                if (playerResult) {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("§6Chance result: §ePLAYER"),
                                        false
                                    );
                                } else {
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("§6Chance result: §eMOB"),
                                        false
                                    );
                                }
                                return 1;
                            })
                        )
        );
    }
}
