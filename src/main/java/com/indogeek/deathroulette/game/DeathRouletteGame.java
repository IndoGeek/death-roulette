package com.indogeek.deathroulette.game;
import net.minecraft.server.MinecraftServer;
public class DeathRouletteGame {
    private static final DeathRouletteGame INSTANCE = new DeathRouletteGame();
    private boolean running = false;
    private long startWorldTime = 0L;
    private long lastProcessedDay = -1;
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
    public void tick(MinecraftServer server) {
        if (!running) {
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
