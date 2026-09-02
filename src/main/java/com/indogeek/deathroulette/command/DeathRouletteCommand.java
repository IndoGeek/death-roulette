package com.indogeek.deathroulette.command;

import com.indogeek.deathroulette.DeathRoulette;
import com.indogeek.deathroulette.game.DeathRouletteGame;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import java.util.List;

import net.minecraft.util.Formatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import static net.minecraft.server.command.CommandManager.argument;
import net.minecraft.text.Text;

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
                        () -> Text.literal("Death Roulette is already running.")
                            .formatted(Formatting.RED),
                        false);
                    return 0;
                  }
                  game.start(context.getSource().getServer());
                  context.getSource().sendFeedback(
                      () -> Text.literal("Death Roulette started!")
                          .formatted(Formatting.GREEN),
                      true);
                  return 1;
                }))
            .then(CommandManager.literal("stop")
                .executes(context -> {
                  DeathRouletteGame game = DeathRouletteGame.getInstance();
                  if (!game.isRunning()) {
                    context.getSource().sendFeedback(
                        () -> Text.literal("Death Roulette is not running.")
                            .formatted(Formatting.RED),
                        false);
                    return 0;
                  }
                  game.stop(context.getSource().getServer());
                  context.getSource().sendFeedback(
                      () -> Text.literal("Death Roulette: ")
                          .formatted(Formatting.GOLD)
                          .append(
                              Text.literal("STOPPED")
                                  .formatted(Formatting.RED)),
                      true);
                  return 1;
                }))
            .then(CommandManager.literal("status")
                .executes(context -> {
                  DeathRouletteGame game = DeathRouletteGame.getInstance();
                  if (game.isRunning()) {
                    List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager()
                        .getPlayerList();
                    StringBuilder playerNames = new StringBuilder();
                    for (ServerPlayerEntity player : players) {
                      if (playerNames.length() > 0) {
                        playerNames.append(", ");
                      }
                      playerNames.append(player.getName().getString());
                    }
                    context.getSource().sendFeedback(
                        () -> Text.literal("Death Roulette: ")
                            .formatted(Formatting.GOLD)
                            .append(
                                Text.literal("RUNNING")
                                    .formatted(Formatting.GREEN))
                            .append(
                                Text.literal(" | Day: ")
                                    .formatted(Formatting.GRAY))
                            .append(
                                Text.literal(
                                    String.valueOf(
                                        game.getElapsedDays(
                                            context.getSource().getServer())))
                                    .formatted(Formatting.YELLOW))
                            .append(
                                Text.literal(" | Ticks: ")
                                    .formatted(Formatting.GRAY))
                            .append(
                                Text.literal(
                                    String.valueOf(
                                        game.getTicksIntoCurrentDay(
                                            context.getSource().getServer())))
                                    .formatted(Formatting.YELLOW))
                            .append(
                                Text.literal(" | Players: ")
                                    .formatted(Formatting.GRAY))
                            .append(
                                Text.literal(
                                    String.valueOf(
                                        game.getOnlinePlayerCount(
                                            context.getSource().getServer())))
                                    .formatted(Formatting.YELLOW))
                            .append(
                                Text.literal(" | Online: ")
                                    .formatted(Formatting.GRAY))
                            .append(
                                Text.literal(playerNames.toString())
                                    .formatted(Formatting.YELLOW)),
                        false);
                  } else {
                    context.getSource().sendFeedback(
                        () -> Text.literal("Death Roulette: ")
                            .formatted(Formatting.GOLD)
                            .append(
                                Text.literal("STOPPED")
                                    .formatted(Formatting.RED)),
                        false);
                  }
                  return 1;
                }))
            .then(CommandManager.literal("reload")
                .executes(context -> {
                  MinecraftServer server = context.getSource().getServer();
                  DeathRouletteGame game = DeathRouletteGame.getInstance();
                  DeathRoulette.CONFIG.load();
                  game.showActionBar(
                      server,
                      Text.literal("Death Roulette configuration reloaded!")
                          .formatted(Formatting.AQUA));
                  DeathRoulette.LOGGER.info("Configuration reloaded.");
                  return 1;
                }))
            .then(CommandManager.literal("test")
                .then(argument("days", IntegerArgumentType.integer(1))
                    .executes(context -> {
                      DeathRouletteGame game = DeathRouletteGame.getInstance();
                      MinecraftServer server = context.getSource().getServer();
                      int days = IntegerArgumentType.getInteger(context, "days");
                      game.fastForwardTest(server, days);
                      context.getSource().sendFeedback(
                          () -> Text.literal("Roulette test: ")
                              .formatted(Formatting.AQUA)
                              .append(
                                  Text.literal(
                                      "fast-forwarded " + days + " day(s).").formatted(Formatting.YELLOW)),
                          true);
                      return 1;
                    }))));
  }
}
