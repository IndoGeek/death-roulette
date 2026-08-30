package com.indogeek.deathroulette.config;

import net.fabricmc.loader.api.FabricLoader;
import com.indogeek.deathroulette.DeathRoulette;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DeathRouletteConfig {

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("deathroulette.properties");


    private static final boolean DEFAULT_ENABLED = true;
    private static final double DEFAULT_PLAYER_CHANCE = 25.0;
    private static final double DEFAULT_MOB_SEARCH_RADIUS = 32.0;
    private static final long DEFAULT_ROULETTE_INTERVAL_DAYS = 10L;

    private static final boolean DEFAULT_ALLOW_NON_OPERATORS = false;

    private static final boolean DEFAULT_SHOW_START_TITLE = true;
    private static final boolean DEFAULT_SHOW_COMPLETION_TITLE = true;
    private static final boolean DEFAULT_SHOW_RESULT_ACTIONBAR = true;
    private static final boolean DEFAULT_SHOW_START_PARTICLES = true;

    private static final boolean DEFAULT_PLAY_START_SOUND = true;
    private static final boolean DEFAULT_PLAY_COUNTDOWN_SOUND = true;
    private static final boolean DEFAULT_PLAY_PLAYER_DEATH_SOUND = true;
    private static final boolean DEFAULT_PLAY_MOB_DEATH_SOUND = true;


    private boolean enabled;
    private double playerChance;
    private double mobSearchRadius;
    private long rouletteIntervalDays;

    private boolean allowNonOperators;

    private boolean showStartTitle;
    private boolean showCompletionTitle;
    private boolean showResultActionbar;
    private boolean showStartParticles;

    private boolean playStartSound;
    private boolean playCountdownSound;
    private boolean playPlayerDeathSound;
    private boolean playMobDeathSound;


    public boolean canUseCommands(ServerCommandSource source) {
        return source.hasPermissionLevel(2) || allowNonOperators;
    }


    public void load() {

        Properties properties = new Properties();

        if (!Files.exists(CONFIG_PATH)) {

            setDefaults();
            save();

            return;
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {

            properties.load(input);

            enabled = parseBoolean(
                    properties,
                    "enabled",
                    DEFAULT_ENABLED
            );

            playerChance = parseDouble(
                    properties,
                    "player_chance",
                    DEFAULT_PLAYER_CHANCE,
                    0.0,
                    100.0
            );

            mobSearchRadius = parseDouble(
                    properties,
                    "mob_search_radius",
                    DEFAULT_MOB_SEARCH_RADIUS,
                    1.0,
                    128.0
            );

            rouletteIntervalDays = parseLong(
                    properties,
                    "roulette_interval_days",
                    DEFAULT_ROULETTE_INTERVAL_DAYS,
                    1L,
                    1000000L
            );

            allowNonOperators = parseBoolean(
                    properties,
                    "allow_non_operators",
                    DEFAULT_ALLOW_NON_OPERATORS
            );

            showStartTitle = parseBoolean(
                    properties,
                    "show_start_title",
                    DEFAULT_SHOW_START_TITLE
            );

            showCompletionTitle = parseBoolean(
                    properties,
                    "show_completion_title",
                    DEFAULT_SHOW_COMPLETION_TITLE
            );

            showResultActionbar = parseBoolean(
                    properties,
                    "show_result_actionbar",
                    DEFAULT_SHOW_RESULT_ACTIONBAR
            );

            showStartParticles = parseBoolean(
                    properties,
                    "show_start_particles",
                    DEFAULT_SHOW_START_PARTICLES
            );

            playStartSound = parseBoolean(
                    properties,
                    "play_start_sound",
                    DEFAULT_PLAY_START_SOUND
            );

            playCountdownSound = parseBoolean(
                    properties,
                    "play_countdown_sound",
                    DEFAULT_PLAY_COUNTDOWN_SOUND
            );

            playPlayerDeathSound = parseBoolean(
                    properties,
                    "play_player_death_sound",
                    DEFAULT_PLAY_PLAYER_DEATH_SOUND
            );

            playMobDeathSound = parseBoolean(
                    properties,
                    "play_mob_death_sound",
                    DEFAULT_PLAY_MOB_DEATH_SOUND
            );

        } catch (IOException e) {

            DeathRoulette.LOGGER.error(
                    "Failed to load configuration: {}",
                    e.getMessage()
            );

            setDefaults();
        }
    }


    private void setDefaults() {

        enabled = DEFAULT_ENABLED;

        playerChance = DEFAULT_PLAYER_CHANCE;
        mobSearchRadius = DEFAULT_MOB_SEARCH_RADIUS;
        rouletteIntervalDays = DEFAULT_ROULETTE_INTERVAL_DAYS;

        allowNonOperators = DEFAULT_ALLOW_NON_OPERATORS;

        showStartTitle = DEFAULT_SHOW_START_TITLE;
        showCompletionTitle = DEFAULT_SHOW_COMPLETION_TITLE;
        showResultActionbar = DEFAULT_SHOW_RESULT_ACTIONBAR;
        showStartParticles = DEFAULT_SHOW_START_PARTICLES;

        playStartSound = DEFAULT_PLAY_START_SOUND;
        playCountdownSound = DEFAULT_PLAY_COUNTDOWN_SOUND;
        playPlayerDeathSound = DEFAULT_PLAY_PLAYER_DEATH_SOUND;
        playMobDeathSound = DEFAULT_PLAY_MOB_DEATH_SOUND;
    }


    public void save() {

        Properties properties = new Properties();

        properties.setProperty(
                "enabled",
                String.valueOf(enabled)
        );

        properties.setProperty(
                "roulette_interval_days",
                String.valueOf(rouletteIntervalDays)
        );

        properties.setProperty(
                "player_chance",
                String.valueOf(playerChance)
        );

        properties.setProperty(
                "mob_search_radius",
                String.valueOf(mobSearchRadius)
        );

        properties.setProperty(
                "allow_non_operators",
                String.valueOf(allowNonOperators)
        );

        properties.setProperty(
                "show_start_title",
                String.valueOf(showStartTitle)
        );

        properties.setProperty(
                "show_completion_title",
                String.valueOf(showCompletionTitle)
        );

        properties.setProperty(
                "show_result_actionbar",
                String.valueOf(showResultActionbar)
        );

        properties.setProperty(
                "show_start_particles",
                String.valueOf(showStartParticles)
        );

        properties.setProperty(
                "play_start_sound",
                String.valueOf(playStartSound)
        );

        properties.setProperty(
                "play_countdown_sound",
                String.valueOf(playCountdownSound)
        );

        properties.setProperty(
                "play_player_death_sound",
                String.valueOf(playPlayerDeathSound)
        );

        properties.setProperty(
                "play_mob_death_sound",
                String.valueOf(playMobDeathSound)
        );

        try {

            Files.createDirectories(CONFIG_PATH.getParent());

            try (OutputStream output =
                         Files.newOutputStream(CONFIG_PATH)) {

                properties.store(
                        output,
                        "Death Roulette Configuration"
                );
            }

        } catch (IOException e) {

            DeathRoulette.LOGGER.error(
                    "Failed to save configuration: {}",
                    e.getMessage()
            );
        }
    }

    private boolean parseBoolean(
            Properties properties,
            String key,
            boolean defaultValue
    ) {

        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        if (
                value.equalsIgnoreCase("true")
                        || value.equalsIgnoreCase("false")
        ) {
            return Boolean.parseBoolean(value);
        }

        DeathRoulette.LOGGER.warn(
                "Invalid {}: {}. Using default: {}",
                key,
                value,
                defaultValue
        );

        return defaultValue;
    }


    private double parseDouble(
            Properties properties,
            String key,
            double defaultValue,
            double min,
            double max
    ) {

        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {

            double parsed = Double.parseDouble(value);

            if (parsed < min || parsed > max) {

                DeathRoulette.LOGGER.warn(
                        "Invalid {}: {}. Using default: {}",
                        key,
                        value,
                        defaultValue
                );

                return defaultValue;
            }

            return parsed;

        } catch (NumberFormatException e) {

            DeathRoulette.LOGGER.warn(
                    "Invalid {}: {}. Using default: {}",
                    key,
                    value,
                    defaultValue
            );

            return defaultValue;
        }
    }


    private long parseLong(
            Properties properties,
            String key,
            long defaultValue,
            long min,
            long max
    ) {

        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {

            long parsed = Long.parseLong(value);

            if (parsed < min || parsed > max) {

                DeathRoulette.LOGGER.warn(
                        "Invalid {}: {}. Using default: {}",
                        key,
                        value,
                        defaultValue
                );

                return defaultValue;
            }

            return parsed;

        } catch (NumberFormatException e) {

            DeathRoulette.LOGGER.warn(
                    "Invalid {}: {}. Using default: {}",
                    key,
                    value,
                    defaultValue
            );

            return defaultValue;
        }
    }


    public boolean isEnabled() {
        return enabled;
    }

    public double getPlayerChance() {
        return playerChance;
    }

    public double getMobSearchRadius() {
        return mobSearchRadius;
    }

    public long getRouletteIntervalDays() {
        return rouletteIntervalDays;
    }

    public boolean isAllowNonOperators() {
        return allowNonOperators;
    }

    public boolean isShowStartTitle() {
        return showStartTitle;
    }

    public boolean isShowCompletionTitle() {
        return showCompletionTitle;
    }

    public boolean isShowResultActionbar() {
        return showResultActionbar;
    }

    public boolean isShowStartParticles() {
        return showStartParticles;
    }

    public boolean isPlayStartSound() {
        return playStartSound;
    }

    public boolean isPlayCountdownSound() {
        return playCountdownSound;
    }

    public boolean isPlayPlayerDeathSound() {
        return playPlayerDeathSound;
    }

    public boolean isPlayMobDeathSound() {
        return playMobDeathSound;
    }
}
