package com.indogeek.deathroulette.game;
import net.minecraft.server.MinecraftServer;
public class DeathRouletteGame {
    private static final DeathRouletteGame INSTANCE = new DeathRouletteGame();
    private static final long TICKS_PER_DAY = 24000L;
    private boolean running = false;
    private long startTick = 0L;
    private long elapsedTicks = 0L;
    private DeathRouletteGame() {
    }
    public static DeathRouletteGame getInstance() {
        return INSTANCE;
    }
    public boolean isRunning() {
        return running;
    }
    public void start(MinecraftServer server) {
        if (running) {
            return;
        }
        startTick = server.getOverworld().getTime();
        elapsedTicks = 0L;
        running = true;
    }
    public void stop() {
        running = false;
        startTick = 0L;
        elapsedTicks = 0L;
    }
    public void tick(MinecraftServer server) {
        if (!running) {
            return;
        }
        long currentTick = server.getOverworld().getTime();
        elapsedTicks = currentTick - startTick;
    }
    public long getElapsedTicks() {
        return elapsedTicks;
    }
    public long getElapsedDays() {
        return elapsedTicks / TICKS_PER_DAY;
    }
    public long getTicksIntoCurrentDay() {
        return elapsedTicks % TICKS_PER_DAY;
    }
}
