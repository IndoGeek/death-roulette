package com.indogeek.deathroulette.game;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;

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
        running = true;
    }
    public void stop() {
        running = false;
    }
    public void announce(MinecraftServer server, String message) {
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
                }
            }
            return;
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
            if (player == null) {
                return "NO_PLAYER";
            }
            String playerName = player.getName().getString();
            player.kill();
            return "PLAYER:" + playerName;
        }
        return "MOB";
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
