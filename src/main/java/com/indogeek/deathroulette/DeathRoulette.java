package com.indogeek.deathroulette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.indogeek.deathroulette.command.DeathRouletteCommand;
import com.indogeek.deathroulette.game.DeathRouletteGame;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.indogeek.deathroulette.config.DeathRouletteConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DeathRoulette implements ModInitializer {
    public static final String MOD_ID = "deathroulette";
    public static final Logger LOGGER =
        LoggerFactory.getLogger("Death Roulette");
    public static final DeathRouletteConfig CONFIG =
        new DeathRouletteConfig();
    @Override
    public void onInitialize() {
        System.out.println("[Death Roulette] Mod loaded!");
        CONFIG.load();
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    DeathRouletteCommand.register(dispatcher);
                }
        );
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DeathRouletteGame game = DeathRouletteGame.getInstance();
            game.loadState(server);
            if (DeathRoulette.CONFIG.isEnabled() && !game.isRunning()) {
                game.start(server);
                DeathRoulette.LOGGER.info(
                    "Death Roulette has been enabled"
                );
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DeathRouletteGame.getInstance().saveState(server);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DeathRouletteGame.getInstance().tick(server);
        });
    }
}
