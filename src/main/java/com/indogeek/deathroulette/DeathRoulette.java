package com.indogeek.deathroulette;
import com.indogeek.deathroulette.command.DeathRouletteCommand;
import com.indogeek.deathroulette.game.DeathRouletteGame;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
public class DeathRoulette implements ModInitializer {
    public static final String MOD_ID = "deathroulette";
    @Override
    public void onInitialize() {
        System.out.println("[Death Roulette] Mod loaded!");
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    DeathRouletteCommand.register(dispatcher);
                }
        );
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DeathRouletteGame.getInstance().tick(server);
        });
    }
}
