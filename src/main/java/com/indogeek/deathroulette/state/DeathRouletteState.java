package com.indogeek.deathroulette.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class DeathRouletteState extends PersistentState {

    private static final String STATE_ID = "deathroulette_state";

    private boolean running = false;
    private long startWorldTime = 0L;
    private long lastProcessedDay = -1L;

    public DeathRouletteState() {
    }

    public static DeathRouletteState fromNbt(NbtCompound nbt) {
        DeathRouletteState state = new DeathRouletteState();

        state.running = nbt.getBoolean("running");
        state.startWorldTime = nbt.getLong("startWorldTime");
        state.lastProcessedDay = nbt.getLong("lastProcessedDay");

        return state;
    }

    public static DeathRouletteState get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();

        PersistentStateManager manager =
                overworld.getPersistentStateManager();

        return manager.getOrCreate(
                DeathRouletteState::fromNbt,
                DeathRouletteState::new,
                STATE_ID
        );
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("running", running);
        nbt.putLong("startWorldTime", startWorldTime);
        nbt.putLong("lastProcessedDay", lastProcessedDay);

        return nbt;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
        markDirty();
    }

    public long getStartWorldTime() {
        return startWorldTime;
    }

    public void setStartWorldTime(long startWorldTime) {
        this.startWorldTime = startWorldTime;
        markDirty();
    }

    public long getLastProcessedDay() {
        return lastProcessedDay;
    }

    public void setLastProcessedDay(long lastProcessedDay) {
        this.lastProcessedDay = lastProcessedDay;
        markDirty();
    }
}
