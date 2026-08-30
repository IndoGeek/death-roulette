package com.indogeek.deathroulette.config;

import com.indogeek.deathroulette.DeathRoulette;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;

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
    private static final boolean DEFAULT_ALLOW_PASSIVE_MOBS = true;
    private static final boolean DEFAULT_ALLOW_HOSTILE_MOBS = true;

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
    private boolean allowPassiveMobs;
    private boolean allowHostileMobs;

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
      
            allowPassiveMobs = parseBoolean(
                properties,
                "allow_passive_mobs",
                DEFAULT_ALLOW_PASSIVE_MOBS
            );

            allowHostileMobs = parseBoolean(
                properties,
                "allow_hostile_mobs",
                DEFAULT_ALLOW_HOSTILE_MOBS
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
            save();

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
        allowPassiveMobs = DEFAULT_ALLOW_PASSIVE_MOBS;
        allowHostileMobs = DEFAULT_ALLOW_HOSTILE_MOBS;

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

        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (java.io.BufferedWriter writer =
                         Files.newBufferedWriter(CONFIG_PATH)) {

                writer.write("# ==========================================");
                writer.newLine();
                writer.write("# Death Roulette Configuration");
                writer.newLine();
                writer.write("# ==========================================");
                writer.newLine();
                writer.newLine();

                writer.write("# Master switch for automatic Death Roulette.");
                writer.newLine();
                writer.write("# true  = Death Roulette runs automatically.");
                writer.newLine();
                writer.write("# false = Death Roulette is disabled.");
                writer.newLine();
                writer.write("enabled=" + enabled);
                writer.newLine();
                writer.newLine();

                writer.write("# Number of Minecraft days between roulette events.");
                writer.newLine();
                writer.write("roulette_interval_days=" + rouletteIntervalDays);
                writer.newLine();
                writer.newLine();

                writer.write("# Chance of selecting a player instead of a mob.");
                writer.newLine();
                writer.write("# 0.0   = always mob");
                writer.newLine();
                writer.write("# 50.0  = 50% player / 50% mob");
                writer.newLine();
                writer.write("# 100.0 = always player");
                writer.newLine();
                writer.write("player_chance=" + playerChance);
                writer.newLine();
                writer.newLine();

                writer.write("# Radius used when searching for nearby mobs.");
                writer.newLine();
                writer.write("mob_search_radius=" + mobSearchRadius);
                writer.newLine();
                writer.newLine();


                writer.write("# ==========================================");
                writer.newLine();
                writer.write("# Command Permissions");
                writer.newLine();
                writer.write("# ==========================================");
                writer.newLine();
                writer.newLine();

                writer.write("# Allow non-operators to use /roulette commands.");
                writer.newLine();
                writer.write("# false = operators only");
                writer.newLine();
                writer.write("# true  = all players can use commands");
                writer.newLine();
                writer.write("allow_non_operators=" + allowNonOperators);
                writer.newLine();
                writer.newLine();


                writer.write("# ==========================================");
                writer.newLine();
                writer.write("# Mob Selection");
                writer.newLine();
                writer.write("# ==========================================");
                writer.newLine();
                writer.newLine();

                writer.write("# Allow passive/non-hostile mobs to be selected.");
                writer.newLine();
                writer.write("allow_passive_mobs=" + allowPassiveMobs);
                writer.newLine();
                writer.newLine();

                writer.write("# Allow hostile mobs to be selected.");
                writer.newLine();
                writer.write("allow_hostile_mobs=" + allowHostileMobs);
                writer.newLine();
                writer.newLine();


                writer.write("# ==========================================");
                writer.newLine();
                writer.write("# Visual Settings");
                writer.newLine();
                writer.write("# ==========================================");
                writer.newLine();
                writer.newLine();

                writer.write("# Show the \"DEATH ROULETTE STARTED\" title.");
                writer.newLine();
                writer.write("show_start_title=" + showStartTitle);
                writer.newLine();
                writer.newLine();

                writer.write("# Show the \"DEATH ROULETTE COMPLETE\" title.");
                writer.newLine();
                writer.write("show_completion_title=" + showCompletionTitle);
                writer.newLine();
                writer.newLine();

                writer.write("# Show the selected player/mob result in the action bar.");
                writer.newLine();
                writer.write("show_result_actionbar=" + showResultActionbar);
                writer.newLine();
                writer.newLine();

                writer.write("# Show Totem of Undying particles when roulette starts.");
                writer.newLine();
                writer.write("show_start_particles=" + showStartParticles);
                writer.newLine();
                writer.newLine();


                writer.write("# ==========================================");
                writer.newLine();
                writer.write("# Sound Settings");
                writer.newLine();
                writer.write("# ==========================================");
                writer.newLine();
                writer.newLine();

                writer.write("# Play the roulette-start sound.");
                writer.newLine();
                writer.write("play_start_sound=" + playStartSound);
                writer.newLine();
                writer.newLine();

                writer.write("# Play the note-block countdown sounds.");
                writer.newLine();
                writer.write("play_countdown_sound=" + playCountdownSound);
                writer.newLine();
                writer.newLine();

                writer.write("# Play the player death sound.");
                writer.newLine();
                writer.write("play_player_death_sound=" + playPlayerDeathSound);
                writer.newLine();
                writer.newLine();

                writer.write("# Play the mob death sound.");
                writer.newLine();
                writer.write("play_mob_death_sound=" + playMobDeathSound);
                writer.newLine();
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
    public boolean isAllowPassiveMobs() {
        return allowPassiveMobs;
    }
    public boolean isAllowHostileMobs() {
        return allowHostileMobs;
    }
}
