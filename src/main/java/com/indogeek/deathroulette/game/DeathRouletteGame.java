package com.indogeek.deathroulette.game;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import net.minecraft.server.world.ServerWorld;
import com.indogeek.deathroulette.DeathRoulette;
import com.indogeek.deathroulette.state.DeathRouletteState;

public class DeathRouletteGame {
    private static final DeathRouletteGame INSTANCE = new DeathRouletteGame();
    private boolean running = false;
    private long startWorldTime = 0L;
    private long lastProcessedDay = -1L;
    private int countdownTicks = 0;
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
                new TitleS2CPacket(
                    Text.literal(message)
                )
            );
        }
    }
    public boolean startCountdown(MinecraftServer server) {
        if (!running) {
            return false;
        }
        if (countdownTicks > 0) {
            return false;
        }
        countdownTicks = 200;
        showTitle(server, "§c10");
        DeathRoulette.LOGGER.info("Countdown started.");
        return true;
    }
    public void tick(MinecraftServer server) {
        if (!running) {
            return;
        }
        if (countdownTicks > 0) {
            countdownTicks--;
            if (countdownTicks % 20 == 0) {
                int seconds = countdownTicks / 20;
                if (seconds > 0) {
                    showTitle(server, "§c" + seconds);
                } else {
                    showTitle(server, "§6DEATH ROULETTE");
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
                        showActionBar(server, "§6Death Roulette: §ePlayer " + playerName + " was killed.");
                        completionMessageTicks = 40;
                        DeathRoulette.LOGGER.info("Player killed successfully.");
                        DeathRoulette.LOGGER.info("Roulette completed.");
                    } else if (result.startsWith("MOB:")) {
                        String mobName = result.substring("MOB:".length());
                        DeathRoulette.LOGGER.info("Result: MOB");
                        DeathRoulette.LOGGER.info("Selected mob: {}", mobName);
                        showActionBar(
                            server,
                            "§6Death Roulette: §e" + mobName + " was killed."
                        );
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
        long currentDay = getElapsedDays(server);
        long interval = DeathRoulette.CONFIG.getRouletteIntervalDays();
        if (currentDay >= lastProcessedDay + interval) {
            lastProcessedDay = currentDay;
            processNewDay(server, currentDay);
            saveLastProcessedDay(server);
        }
    }
    private void saveLastProcessedDay(MinecraftServer server) {
        DeathRouletteState state = DeathRouletteState.get(server);
        state.setLastProcessedDay(lastProcessedDay);
    }
    private void processNewDay(MinecraftServer server, long day) {
        server.sendMessage(
            net.minecraft.text.Text.literal(
                "$6Death Roulette: $eDay " + day + " has begun"
            )
        );
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
        List<ServerPlayerEntity> players = getOnlinePlayers(server);
        if (players.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * players.size());
            return players.get(randomIndex);
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
        );
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
    public MobEntity killRandomNearbyMob(MinecraftServer server) {
        MobEntity mob = getRandomNearbyMob(server);
        if (mob == null) {
            return null;
        }
        mob.kill();
        return mob;
    }
    public String executeRoulette(MinecraftServer server) {
        if (getOnlinePlayers(server).isEmpty()) {
            return "NO_PLAYER";
        }
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
        MobEntity mob = killRandomNearbyMob(server);
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
