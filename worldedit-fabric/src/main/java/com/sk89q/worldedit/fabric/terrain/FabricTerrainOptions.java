/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 * Copyright (C) 2026 jaehyun <ano701.main@gmail.com> (Motopoly-derived terrain code)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sk89q.worldedit.fabric.terrain;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

/** Per-player terrain generation options persisted by UUID. */
public record FabricTerrainOptions(int distance, int round, double rough, double step) {
    public static final FabricTerrainOptions DEFAULT = new FabricTerrainOptions(10, 0, Double.NaN, 1.0);

    public FabricTerrainOptions {
        if (distance < 0 || distance > 64 || round < 0 || round > 100
            || (!Double.isNaN(rough) && (!Double.isFinite(rough) || rough < 0 || rough > 16))
            || !Double.isFinite(step) || step < 0 || step > 16) {
            throw new IllegalArgumentException("Terrain option is outside its accepted range");
        }
    }

    public static FabricTerrainOptions load(Path configDir, UUID playerId) throws IOException {
        Path file = file(configDir, playerId);
        if (!Files.exists(file)) return DEFAULT;
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) { values.load(reader); }
        try {
            return new FabricTerrainOptions(
                Integer.parseInt(values.getProperty("distance", "10")),
                Integer.parseInt(values.getProperty("round", "0")),
                parseRough(values.getProperty("rough", "auto")),
                Double.parseDouble(values.getProperty("step", "1.0"))
            );
        } catch (RuntimeException ignored) {
            return DEFAULT;
        }
    }

    public void save(Path configDir, UUID playerId) throws IOException {
        Path file = file(configDir, playerId);
        Files.createDirectories(file.getParent());
        Properties values = new Properties();
        values.setProperty("distance", Integer.toString(distance));
        values.setProperty("round", Integer.toString(round));
        values.setProperty("rough", Double.isNaN(rough) ? "auto" : Double.toString(rough));
        values.setProperty("step", Double.toString(step));
        try (Writer writer = Files.newBufferedWriter(file)) { values.store(writer, "Polixit terrain options"); }
    }

    public static double parseRough(String value) {
        if (value.equalsIgnoreCase("auto")) return Double.NaN;
        double rough = Double.parseDouble(value);
        if (!Double.isFinite(rough) || rough < 0 || rough > 16) throw new IllegalArgumentException("Invalid roughness");
        return rough;
    }

    private static Path file(Path configDir, UUID playerId) {
        return configDir.resolve("polixit").resolve("terrain-players").resolve(playerId + ".properties");
    }
}
