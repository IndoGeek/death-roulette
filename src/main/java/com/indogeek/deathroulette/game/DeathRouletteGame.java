package com.indogeek.deathroulette.game;

import com.indogeek.deathroulette.DeathRoulette;
import com.indogeek.deathroulette.state.DeathRouletteState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
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

public class DeathRouletteGame {
    private static final DeathRouletteGame INSTANCE = new DeathRouletteGame();
    private boolean running = false;
    private long startWorldTime = 0L;
    private long lastProcessedDay = -1L;
    private int countdownTicks = 0;
    private int startAnnouncementTicks = 0;
    private int completionMessageTicks = 0;
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
        completionMessageTicks = 0;
        DeathRoulette.LOGGER.info(
            "State restored: running={}, startWorldTime={}, lastProcessedDay={}",
            running,
            startWorldTime,
            lastProcessedDay
        );
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
            lastProcessedDay
        );
    }
    public void start(MinecraftServer server) {
        startWorldTime = server.getOverworld().getTime();
        lastProcessedDay = 0L;
        countdownTicks = 0;
        startAnnouncementTicks = 0;
        completionMessageTicks = 0;
        running = true;
        saveState(server);
        DeathRoulette.LOGGER.info("Roulette started.");
    }
    public void startTest(MinecraftServer server) {
        startWorldTime = server.getOverworld().getTime() - (10L * 24000L);
        lastProcessedDay = 10;
        countdownTicks = 0;
        running = true;
        DeathRoulette.LOGGER.info("Test roulette started. Forced to Day 10.");
    }
    public void stop(MinecraftServer server) {
        running = false;
        countdownTicks = 0;
        startAnnouncementTicks = 0;
        completionMessageTicks = 0;
        saveState(server);
        DeathRoulette.LOGGER.info("Roulette stopped.");
    }
    public void announce(MinecraftServer server, String message) {
        for (ServerPlayerEntity player : getOnlinePlayers(server)) {
            player.sendMessage(
                Text.literal(message),
                false
            );
        }
    }
    public void showActionBar(MinecraftServer server, String message) {
        for (ServerPlayerEntity player : getOnlinePlayers(server)) {
            player.sendMessage(
                Text.literal(message),
                true
            );
        }
    }
    public void showTitle(MinecraftServer server, String message) {
        for (ServerPlayerEntity player : getOnlinePlayers(server)) {
            player.networkHandler.sendPacket(
                new TitleFadeS2CPacket(10, 80, 10)
            );
            player.networkHandler.sendPacket(
                new TitleS2CPacket(
                    Text.literal(message)
                )
            );
        }
    }
    public void playSoundToEveryone(
            MinecraftServer server,
            net.minecraft.sound.SoundEvent sound,
            float volume,
            float pitch
    ) {
        for (ServerPlayerEntity player : getOnlinePlayers(server)) {
            player.playSound(
                sound,
                SoundCategory.MASTER,
                volume,
                pitch
            );
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
                0.2
            );
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
            showTitle(server, "§cDEATH ROULETTE STARTED");
        }
        if (DeathRoulette.CONFIG.isShowStartParticles()) {
            playStartEffect(server);
        }
        if (DeathRoulette.CONFIG.isPlayStartSound()) {
            playSoundToEveryone(
                server,
                SoundEvents.ITEM_TOTEM_USE,
                1.0f,
                0.6f
            );
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
                showTitle(server, "§c10");
                DeathRoulette.LOGGER.info("Countdown started.");
            }
            return;
        }
        if (countdownTicks > 0) {
            countdownTicks--;
            if (countdownTicks % 20 == 0) {
                int seconds = countdownTicks / 20;
                if (seconds > 0) {
                    showTitle(server, "§c" + seconds);
                    if (DeathRoulette.CONFIG.isPlayCountdownSound()) {
                        playSoundToEveryone(
                            server,
                            SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                            1.0f,
                            1.0f
                        );
                    }
                } else {
                    countdownTicks = 0;
                    String result = executeRoulette(server);
                    if (result.equals("NO_PLAYER")) {
                        showActionBar(server, "§cDeath Roulette failed: No online players found.");
                        DeathRoulette.LOGGER.warn("Failed: No online players found.");
                        DeathRoulette.LOGGER.info("Roulette completed: WITH FAILURE.");
                    } else if (result.startsWith("PLAYER:")) {
                        String playerName = result.substring("PLAYER:".length());
                        DeathRoulette.LOGGER.info("Result: PLAYER");
                        DeathRoulette.LOGGER.info("Selected player: {}", playerName);
                        if (DeathRoulette.CONFIG.isShowCompletionTitle()) {
                            showTitle(server, "§6DEATH ROULETTE COMPLETE");
                        }
                        if (DeathRoulette.CONFIG.isPlayPlayerDeathSound()) {
                            playSoundToEveryone(
                                server,
                                SoundEvents.ENTITY_PLAYER_DEATH,
                                1.0f,
                                1.0f
                            );
                        }
                        if (DeathRoulette.CONFIG.isShowResultActionbar()) {
                            showActionBar(server, "§6Death Roulette: §ePlayer " + playerName + " was killed.");
                        }
                        completionMessageTicks = 40;
                        DeathRoulette.LOGGER.info("Player killed successfully.");
                        DeathRoulette.LOGGER.info("Roulette completed.");
                    } else if (result.startsWith("MOB:")) {
                        String mobName = result.substring("MOB:".length());
                        DeathRoulette.LOGGER.info("Result: MOB");
                        DeathRoulette.LOGGER.info("Selected mob: {}", mobName);
                        if (DeathRoulette.CONFIG.isShowCompletionTitle()) {
                            showTitle(server, "§6DEATH ROULETTE COMPLETE");
                        }
                        if (DeathRoulette.CONFIG.isPlayMobDeathSound()) {
                            playSoundToEveryone(
                                server,
                                SoundEvents.ENTITY_GENERIC_DEATH,
                                1.0f,
                                1.0f
                            );
                        }
                        if (DeathRoulette.CONFIG.isShowResultActionbar()) {
                            showActionBar(
                                server,
                                "§6Death Roulette: §e" + mobName + " was killed."
                            );
                        }
                        completionMessageTicks = 40;
                        DeathRoulette.LOGGER.info("Mob killed successfully.");
                        DeathRoulette.LOGGER.info("Roulette completed.");
                    } else if (result.equals("NO_MOB")) {
                        showActionBar(
                            server,
                            "§cDeath Roulette failed: No nearby mobs found."
                        );
                        DeathRoulette.LOGGER.warn("Failed: No nearby mobs found.");
                        DeathRoulette.LOGGER.info("Roulette completed: WITH FAILURE.");
                    }
                }
            }
        }
        if (completionMessageTicks > 0) {
            completionMessageTicks--;
            if (completionMessageTicks == 0) {
                showActionBar(server, "§aDeath Roulette complete!");
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
            Text.literal(
                "$6Death Roulette: $eDay " + day + " has begun"
            )
        );
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
        double radius =
            DeathRoulette.CONFIG.getMobSearchRadius();
        Box searchBox = new Box(
            centerPlayer.getX() - radius,
            centerPlayer.getY() - radius,
            centerPlayer.getZ() - radius,
            centerPlayer.getX() + radius,
            centerPlayer.getY() + radius,
            centerPlayer.getZ() + radius
        );
        return world.getEntitiesByClass(
            MobEntity.class,
            searchBox,
            mob -> mob.isAlive()
                    && isAllowedMob(mob)
        );
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
        return Math.random() <
            (DeathRoulette.CONFIG.getPlayerChance() / 100.0);
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
