package com.indogeek.deathroulette.config;

import net.fabricmc.loader.api.FabricLoader;
import com.indogeek.deathroulette.DeathRoulette;

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

    private static final double DEFAULT_PLAYER_CHANCE = 25.0;
    private static final double DEFAULT_MOB_SEARCH_RADIUS = 32.0;
    private static final long DEFAULT_ROULETTE_INTERVAL_DAYS = 1L;

    private double playerChance;
    private double mobSearchRadius;
    private long rouletteIntervalDays;

    public void load() {
        Properties properties = new Properties();

        if (!Files.exists(CONFIG_PATH)) {
            playerChance = DEFAULT_PLAYER_CHANCE;
            mobSearchRadius = DEFAULT_MOB_SEARCH_RADIUS;
            rouletteIntervalDays = DEFAULT_ROULETTE_INTERVAL_DAYS;

            save();
            return;
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);

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

        } catch (IOException e) {
            DeathRoulette.LOGGER.error(
                "Failed to load configuration: {}",
                e.getMessage()
            );
            playerChance = DEFAULT_PLAYER_CHANCE;
            mobSearchRadius = DEFAULT_MOB_SEARCH_RADIUS;
            rouletteIntervalDays = DEFAULT_ROULETTE_INTERVAL_DAYS;
        }
    }

    public void save() {
        Properties properties = new Properties();

        properties.setProperty(
                "player_chance",
                String.valueOf(playerChance)
        );

        properties.setProperty(
                "mob_search_radius",
                String.valueOf(mobSearchRadius)
        );

        properties.setProperty(
                "roulette_interval_days",
                String.valueOf(rouletteIntervalDays)
        );

        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
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
                System.err.println(
                        "[Death Roulette] Invalid " + key
                                + ": " + value
                                + ". Using default: "
                                + defaultValue
                );

                return defaultValue;
            }

            return parsed;

        } catch (NumberFormatException e) {
            System.err.println(
                    "[Death Roulette] Invalid " + key
                            + ": " + value
                            + ". Using default: "
                            + defaultValue
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
                System.err.println(
                        "[Death Roulette] Invalid " + key
                                + ": " + value
                                + ". Using default: "
                                + defaultValue
                );

                return defaultValue;
            }

            return parsed;

        } catch (NumberFormatException e) {
            System.err.println(
                    "[Death Roulette] Invalid " + key
                            + ": " + value
                            + ". Using default: "
                            + defaultValue
            );

            return defaultValue;
        }
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
}
