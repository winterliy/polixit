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
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit.terrain

import org.bukkit.configuration.file.FileConfiguration

data class Settings(
    val blocksPerTick: Int,
    val maxColumns: Int,
    val analysisMargin: Int,
    val undoDepth: Int,
    val maxFillDepth: Int,
    /** Width of the transition band outside the selection; players override it with /mt config distance. */
    val defaultDistance: Int,
    /** Corner rounding 0~100; players override it with /mt config round. */
    val defaultRound: Int,
    /** Undulation in blocks, NaN = auto; players override it with /mt config rough. */
    val defaultRough: Double,
    val scale: Double,
    /** Shape and dress new ground after what vanilla generates there, computed on the spot. */
    val vanillaTerrain: Boolean,
    val copyPlants: Boolean,
    /** Default steepest height difference between neighbouring columns (0 = off); players override it with /mt config step. */
    val maxStep: Double,
    /** How far sand reaches inland from water. */
    val beachWidth: Int,
    /** Scatter vanilla-like ore veins through newly filled stone. */
    val fillOres: Boolean,
) {
    companion object {
        fun load(config: FileConfiguration) = Settings(
            blocksPerTick = config.getInt("blocks-per-tick", 20000).coerceAtLeast(100),
            maxColumns = config.getInt("max-columns", 262144).coerceAtLeast(1),
            analysisMargin = config.getInt("analysis-margin", 8).coerceIn(1, 64),
            undoDepth = config.getInt("undo-depth", 5).coerceAtLeast(0),
            maxFillDepth = config.getInt("max-fill-depth", 64).coerceIn(1, 512),
            defaultDistance = config.getInt("defaults.distance", config.getInt("defaults.smooth", 10)).coerceIn(0, 64),
            defaultRound = config.getInt("defaults.round", 0).coerceIn(0, 100),
            defaultRough = parseRough(config.getString("defaults.rough", "auto")!!) ?: Double.NaN,
            scale = config.getDouble("scale", 48.0).coerceAtLeast(1.0),
            vanillaTerrain = config.getBoolean("vanilla-terrain", config.getBoolean("reference-terrain", true)),
            copyPlants = config.getBoolean("copy-plants", true),
            maxStep = config.getInt("max-step", 1).coerceIn(0, 16).toDouble(),
            beachWidth = config.getInt("beach-width", 5).coerceIn(0, 32),
            fillOres = config.getBoolean("fill-ores", true),
        )

        /** "auto" → NaN, a non-negative number → itself, anything else → null. */
        fun parseRough(value: String): Double? =
            if (value.equals("auto", ignoreCase = true)) Double.NaN
            else value.toDoubleOrNull()?.takeIf { it >= 0 }
    }
}
