package com.indogeek.deathroulette.game;

import com.indogeek.deathroulette.DeathRoulette;
import com.indogeek.deathroulette.state.DeathRouletteState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;
import net.minecraft.util.Formatting;

public class DeathRouletteGame {
  private static final DeathRouletteGame INSTANCE = new DeathRouletteGame();
  private boolean running = false;
  private long startWorldTime = 0L;
  private long lastProcessedDay = -1L;
  private int countdownTicks = 0;
  private int startAnnouncementTicks = 0;

  private DeathRouletteGame() {
  }

  public static DeathRouletteGame getInstance() {
    return INSTANCE;
  }

  public boolean isRunning() {
    return running;
  }

  public void loadState(MinecraftServer server) {
    DeathRouletteState state = DeathRouletteState.get(server);
    running = state.isRunning();
    startWorldTime = state.getStartWorldTime();
    lastProcessedDay = state.getLastProcessedDay();
    countdownTicks = 0;
    startAnnouncementTicks = 0;
    DeathRoulette.LOGGER.info(
        "State restored: running={}, startWorldTime={}, lastProcessedDay={}",
        running,
        startWorldTime,
        lastProcessedDay);
  }

  public void saveState(MinecraftServer server) {
    DeathRouletteState state = DeathRouletteState.get(server);
    state.setRunning(running);
    state.setStartWorldTime(startWorldTime);
    state.setLastProcessedDay(lastProcessedDay);
    state.markDirty();
    DeathRoulette.LOGGER.info(
        "State saved: running={}, startWorldTime={}, lastProcessedDay={}",
        running,
        startWorldTime,
        lastProcessedDay);
  }

  public void start(MinecraftServer server) {
    startWorldTime = server.getOverworld().getTime();
    lastProcessedDay = 0L;
    countdownTicks = 0;
    startAnnouncementTicks = 0;
    running = true;
    saveState(server);
    DeathRoulette.LOGGER.info("Roulette started.");
  }

  public void fastForwardTest(MinecraftServer server, int days) {
    if (days < 1) {
      return;
    }
    startWorldTime -= (long) days * 24000L;
    DeathRoulette.LOGGER.info(
        "Test roulette fast-forwarded by {} day(s). Current day: {}",
        days,
        getElapsedDays(server));
    tick(server);
  }

  public void stop(MinecraftServer server) {
    running = false;
    countdownTicks = 0;
    startAnnouncementTicks = 0;
    saveState(server);
    DeathRoulette.LOGGER.info("Roulette stopped.");
  }

  public void announce(MinecraftServer server, String message) {
    for (ServerPlayerEntity player : getOnlinePlayers(server)) {
      player.sendMessage(
          Text.literal(message),
          false);
    }
  }

  public void showActionBar(MinecraftServer server, Text message) {
    for (ServerPlayerEntity player : getOnlinePlayers(server)) {
      player.sendMessage(
          message,
          true);
    }
  }

  public void showTitle(MinecraftServer server, Text title) {
    for (ServerPlayerEntity player : getOnlinePlayers(server)) {
      player.networkHandler.sendPacket(
          new TitleFadeS2CPacket(5, 30, 10));
      player.networkHandler.sendPacket(
          new TitleS2CPacket(title));
    }
  }

  public void showSubtitle(MinecraftServer server, Text subtitle) {
    for (ServerPlayerEntity player : getOnlinePlayers(server)) {
      player.networkHandler.sendPacket(
          new SubtitleS2CPacket(subtitle));
    }
  }

  public void playSoundToEveryone(
      MinecraftServer server,
      net.minecraft.sound.SoundEvent sound,
      float volume,
      float pitch) {
    for (ServerPlayerEntity player : getOnlinePlayers(server)) {
      player.playSound(
          sound,
          SoundCategory.MASTER,
          volume,
          pitch);
    }
  }

  public void playStartEffect(MinecraftServer server) {
    for (ServerPlayerEntity player : getOnlinePlayers(server)) {
      ServerWorld world = player.getServerWorld();
      world.spawnParticles(
          ParticleTypes.TOTEM_OF_UNDYING,
          player.getX(),
          player.getY() + 1.0,
          player.getZ(),
          40,
          0.8,
          1.0,
          0.8,
          0.2);
    }
  }

  public boolean startCountdown(MinecraftServer server) {
    if (!running) {
      return false;
    }
    if (countdownTicks > 0 || startAnnouncementTicks > 0) {
      return false;
    }
    startAnnouncementTicks = 80;
    if (DeathRoulette.CONFIG.isShowStartTitle()) {
      showTitle(
          server,
          Text.literal("DEATH ROULETTE")
              .formatted(Formatting.DARK_RED, Formatting.BOLD));
      showSubtitle(
          server,
          Text.literal("The wheel is turning...")
              .formatted(Formatting.RED));
    }
    if (DeathRoulette.CONFIG.isShowStartParticles()) {
      playStartEffect(server);
    }
    if (DeathRoulette.CONFIG.isPlayStartSound()) {
      playSoundToEveryone(
          server,
          SoundEvents.ITEM_TOTEM_USE,
          1.0f,
          0.6f);
    }
    DeathRoulette.LOGGER.info("Roulette announcement started.");
    return true;
  }

  public void tick(MinecraftServer server) {
    if (!running) {
      return;
    }
    if (startAnnouncementTicks > 0) {
      startAnnouncementTicks--;
      if (startAnnouncementTicks == 0) {
        countdownTicks = 200;
        showTitle(
            server,
            Text.literal("10")
                .formatted(Formatting.RED, Formatting.BOLD));
      }
      return;
    }
    if (countdownTicks > 0) {
      countdownTicks--;
      if (countdownTicks % 20 == 0) {
        int seconds = countdownTicks / 20;
        if (seconds > 0) {
          showTitle(
              server,
              Text.literal(String.valueOf(seconds))
                  .formatted(Formatting.RED, Formatting.BOLD));
          showSubtitle(
              server,
              Text.literal("Prepare yourself...")
                  .formatted(Formatting.GRAY));
          if (DeathRoulette.CONFIG.isPlayCountdownSound()) {
            playSoundToEveryone(
                server,
                SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                1.0f,
                seconds <= 3 ? 0.7f : 1.0f);
          }
        } else {
          countdownTicks = 0;
          showTitle(
              server,
              Text.literal("ROLL!")
                  .formatted(Formatting.DARK_RED, Formatting.BOLD));
          showSubtitle(
              server,
              Text.literal("Death Roulette has chosen...")
                  .formatted(Formatting.RED));
          String result = executeRoulette(server);
          if (result.equals("NO_PLAYER")) {
            showActionBar(
                server,
                Text.literal("Death Roulette failed: ")
                    .formatted(Formatting.RED)
                    .append(
                        Text.literal("No active players found..")
                            .formatted(Formatting.YELLOW)));
            DeathRoulette.LOGGER.warn(
                "Roulette failed: no active players found.");
          } else if (result.startsWith("PLAYER:")) {
            String playerName = result.substring("PLAYER:".length());
            if (DeathRoulette.CONFIG.isShowCompletionTitle()) {
              showTitle(
                  server,
                  Text.literal("ROULETTE COMPLETE")
                      .formatted(Formatting.GOLD, Formatting.BOLD));
            }
            if (DeathRoulette.CONFIG.isPlayPlayerDeathSound()) {
              playSoundToEveryone(
                  server,
                  SoundEvents.ENTITY_ENDER_DRAGON_GROWL,
                  1.0f,
                  1.0f);
            }
            if (DeathRoulette.CONFIG.isShowResultActionbar()) {
              showActionBar(
                  server,
                  Text.literal("Death Roulette: ")
                      .formatted(Formatting.GOLD)
                      .append(
                          Text.literal("Player " + playerName + " was killed.")
                              .formatted(Formatting.YELLOW)));
            }
            DeathRoulette.LOGGER.info(
                "Roulette result: PLAYER | Selected: {} | Status: KILLED",
                playerName);
          } else if (result.startsWith("MOB:")) {
            String mobName = result.substring("MOB:".length());
            if (DeathRoulette.CONFIG.isShowCompletionTitle()) {
              showTitle(
                  server,
                  Text.literal("ROULETTE COMPLETE")
                      .formatted(Formatting.GOLD, Formatting.BOLD));
            }
            if (DeathRoulette.CONFIG.isPlayMobDeathSound()) {
              playSoundToEveryone(
                  server,
                  SoundEvents.ENTITY_GENERIC_DEATH,
                  1.0f,
                  1.0f);
            }
            if (DeathRoulette.CONFIG.isShowResultActionbar()) {
              showActionBar(
                  server,
                  Text.literal("Death Roulette: ")
                      .formatted(Formatting.GOLD)
                      .append(
                          Text.literal(mobName + " was killed.")
                              .formatted(Formatting.YELLOW)));
            }
            DeathRoulette.LOGGER.info(
                "Roulette result: MOB | Selected: {} | Status: KILLED",
                mobName);
          } else if (result.equals("NO_MOB")) {
            showActionBar(
                server,
                Text.literal("Death Roulette failed: ")
                    .formatted(Formatting.RED)
                    .append(
                        Text.literal("No nearby mobs found..")
                            .formatted(Formatting.YELLOW)));
            DeathRoulette.LOGGER.warn(
                "Roulette failed: no nearby mobs found.");
          }
        }
      }
    }
    if (DeathRoulette.CONFIG.isEnabled()) {
      long currentDay = getElapsedDays(server);
      long interval = DeathRoulette.CONFIG.getRouletteIntervalDays();
      if (currentDay >= lastProcessedDay + interval) {
        lastProcessedDay = currentDay;
        processNewDay(server, currentDay);
        saveLastProcessedDay(server);
      }
    }
  }

  private void saveLastProcessedDay(MinecraftServer server) {
    DeathRouletteState state = DeathRouletteState.get(server);
    state.setLastProcessedDay(lastProcessedDay);
  }

  private void processNewDay(MinecraftServer server, long day) {
    server.sendMessage(
        Text.literal("Death Roulette: ")
            .formatted(Formatting.GOLD)
            .append(
                Text.literal("Day " + day + " has begun")
                    .formatted(Formatting.YELLOW)));
    startCountdown(server);
  }

  public void processTestDay(MinecraftServer server) {
    long nextDay = lastProcessedDay + 1;
    lastProcessedDay = nextDay;
    processNewDay(server, nextDay);
  }

  public int getOnlinePlayerCount(MinecraftServer server) {
    return server.getPlayerManager().getPlayerList().size();
  }

  public List<ServerPlayerEntity> getOnlinePlayers(MinecraftServer server) {
    return server.getPlayerManager().getPlayerList();
  }

  public ServerPlayerEntity getRandomPlayer(MinecraftServer server) {
    List<ServerPlayerEntity> survivalPlayers = new ArrayList<>();
    for (ServerPlayerEntity player : getOnlinePlayers(server)) {
      if (player.interactionManager.getGameMode() == GameMode.SURVIVAL
          && player.isAlive()) {
        survivalPlayers.add(player);
      }
    }
    if (survivalPlayers.isEmpty()) {
      return null;
    }
    Random random = new Random();
    return survivalPlayers.get(random.nextInt(survivalPlayers.size()));
  }

  public List<MobEntity> getNearbyMobs(MinecraftServer server) {
    ServerPlayerEntity centerPlayer = getRandomPlayer(server);
    if (centerPlayer == null) {
      return new ArrayList<>();
    }
    ServerWorld world = centerPlayer.getServerWorld();
    double radius = DeathRoulette.CONFIG.getMobSearchRadius();
    Box searchBox = new Box(
        centerPlayer.getX() - radius,
        centerPlayer.getY() - radius,
        centerPlayer.getZ() - radius,
        centerPlayer.getX() + radius,
        centerPlayer.getY() + radius,
        centerPlayer.getZ() + radius);
    return world.getEntitiesByClass(
        MobEntity.class,
        searchBox,
        mob -> mob.isAlive()
            && isAllowedMob(mob));
  }

  private boolean isAllowedMob(MobEntity mob) {
    boolean isHostile = mob instanceof HostileEntity;
    if (isHostile) {
      return DeathRoulette.CONFIG.isAllowHostileMobs();
    }
    return DeathRoulette.CONFIG.isAllowPassiveMobs();
  }

  public MobEntity getRandomNearbyMob(MinecraftServer server) {
    List<MobEntity> mobs = getNearbyMobs(server);
    if (mobs.isEmpty()) {
      return null;
    }
    Random random = new Random();
    return mobs.get(random.nextInt(mobs.size()));
  }

  public boolean killRandomPlayer(MinecraftServer server) {
    ServerPlayerEntity player = getRandomPlayer(server);
    if (player == null) {
      return false;
    }
    player.kill();
    return true;
  }

  public String executeRoulette(MinecraftServer server) {
    boolean playerResult = isPlayerResult();
    if (playerResult) {
      ServerPlayerEntity player = getRandomPlayer(server);
      if (player == null || !player.isAlive()) {
        return "NO_PLAYER";
      }
      String playerName = player.getName().getString();
      player.kill();
      return "PLAYER:" + playerName;
    }
    MobEntity mob = getRandomNearbyMob(server);
    if (mob == null || !mob.isAlive()) {
      return "NO_MOB";
    }
    String mobName = mob.getType().getName().getString();
    mob.kill();
    return "MOB:" + mobName;
  }

  public boolean isPlayerResult() {
    return Math.random() < (DeathRoulette.CONFIG.getPlayerChance() / 100.0);
  }

  public long getElapsedDays(MinecraftServer server) {
    long worldTime = server.getOverworld().getTime();
    long elapsedTime = worldTime - startWorldTime;
    return elapsedTime / 24000L;
  }

  public long getTicksIntoCurrentDay(MinecraftServer server) {
    long worldTime = server.getOverworld().getTime();
    long elapsedTime = worldTime - startWorldTime;
    return elapsedTime % 24000L;
  }
}
