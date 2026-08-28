package com.indogeek.deathroulette.game;
import net.minecraft.server.MinecraftServer;
public class DeathRouletteGame {
    private static final DeathRouletteGame INSTANCE = new DeathRouletteGame();
    private boolean running = false;
    private DeathRouletteGame() {
    }
    public static DeathRouletteGame getInstance() {
        return INSTANCE;
    }
    public boolean isRunning() {
        return running;
    }
    public void start(MinecraftServer server) {
        running = true;
    }
    public void stop() {
        running = false;
    }
    public void tick(MinecraftServer server) {
        if (!running) {
            return;
        }
    }
    public long getElapsedDays(MinecraftServer server) {
        long worldTime = server.getOverworld().getTime();
        return worldTime / 24000L;
    }
    public long getTicksIntoCurrentDay(MinecraftServer server) {
        long worldTime = server.getOverworld().getTime();
        return worldTime % 24000L;
    }
}
