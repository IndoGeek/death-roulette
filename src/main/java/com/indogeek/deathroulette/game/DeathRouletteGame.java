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

public class DeathRouletteGame {
    private static final DeathRouletteGame INSTANCE = new DeathRouletteGame();
    private boolean running = false;
    private long startWorldTime = 0L;
    private long lastProcessedDay = -1;
    private int countdownTicks = 0;
    private double playerChance = 0.50;
    private DeathRouletteGame() {
    }
    public static DeathRouletteGame getInstance() {
        return INSTANCE;
    }
    public boolean isRunning() {
        return running;
    }
    public void start(MinecraftServer server) {
        startWorldTime = server.getOverworld().getTime();
        lastProcessedDay = 0;
        countdownTicks = 0;
        running = true;
    }
    public void startTest(MinecraftServer server) {
        startWorldTime = server.getOverworld().getTime() - (10L * 24000L);
        lastProcessedDay = 10;
        countdownTicks = 0;
        running = true;
    }
    public void stop() {
        running = false;
    }
    public void announce(MinecraftServer server, String message) {
        for (ServerPlayerEntity player : getOnlinePlayers(server)) {
            player.sendMessage(
                Text.literal(message),
                false
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
    public void startCountdown(MinecraftServer server) {
        if (countdownTicks > 0) {
            return;
        }
        countdownTicks = 200;
        showTitle(server, "§c10");
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
                        announce(server, "§cDeath Roulette failed: No online players found.");
                    } else if (result.startsWith("PLAYER:")) {
                        String playerName = result.substring("PLAYER:".length());
                        System.out.println("[Death Roulette] Result: PLAYER");
                        System.out.println("[Death Roulette] Selected player: " + playerName);
                        announce(
                            server,
                            "§6Death Roulette: §ePlayer " + playerName + " was killed."
                        );
                        announce(
                            server,
                            "§aDeath Roulette complete!"
                        );
                        System.out.println("[Death Roulette] Player killed successfully.");
                    } else if (result.startsWith("MOB:")) {
                        String mobName = result.substring("MOB:".length());
                        System.out.println("[Death Roulette] Result: MOB");
                        System.out.println("[Death Roulette] Selected mob: " + mobName);
                        announce(
                            server,
                            "§6Death Roulette: §e" + mobName + " was killed."
                        );
                        announce(
                            server,
                            "§aDeath Roulette complete!"
                        );
                        System.out.println("[Death Roulette] Mob killed successfully.");
                    } else if (result.equals("NO_MOB")) {
                        announce(
                            server,
                            "§cDeath Roulette failed: No nearby mobs found."
                        );
                    }
                }
            }
        }
        long currentDay = getElapsedDays(server);
        if (currentDay > lastProcessedDay) {
            lastProcessedDay = currentDay;
            processNewDay(server, currentDay);
        }
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
        double radius = 32.0;
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
        boolean playerResult = isPlayerResult();
        if (playerResult) {
            ServerPlayerEntity player = getRandomPlayer(server);
            if (player == null) {
                return "NO_PLAYER";
            }
            String playerName = player.getName().getString();
            player.kill();
            return "PLAYER:" + playerName;
        }
        MobEntity mob = killRandomNearbyMob(server);
        if (mob == null) {
            return "NO_MOB";
        }
        String mobName = mob.getType().getName().getString();
        return "MOB:" + mobName;
    }
    public boolean isPlayerResult() {
        return Math.random() < playerChance;
    }
    public void setPlayerChance(double chance) {
        playerChance = chance;
    }
    public double getPlayerChance() {
        return playerChance;
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
