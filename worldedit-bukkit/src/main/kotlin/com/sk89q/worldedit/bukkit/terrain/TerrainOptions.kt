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

import com.sk89q.worldedit.bukkit.BukkitPlayer
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extension.platform.Actor
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/** Per-player generation options, with server defaults supplied by [Settings]. */
data class TerrainOptions(
    val distance: Int,
    val round: Int,
    val rough: Double,
    val step: Double,
)

/** Persists players' terrain options outside WorldEdit's own configuration files. */
class TerrainOptionsStore(private val plugin: WorldEditPlugin, private val defaults: Settings) {

    fun load(actor: Actor): TerrainOptions {
        val file = fileFor(actor) ?: return defaultOptions()
        val config = YamlConfiguration.loadConfiguration(file)
        return TerrainOptions(
            distance = config.getInt("distance", defaults.defaultDistance).coerceIn(0, 64),
            round = config.getInt("round", defaults.defaultRound).coerceIn(0, 100),
            rough = Settings.parseRough(config.getString("rough", "auto")!!)
                ?: defaults.defaultRough,
            step = config.getDouble("step", defaults.maxStep).coerceIn(0.0, 16.0),
        )
    }

    fun save(actor: Actor, options: TerrainOptions): Boolean {
        val file = fileFor(actor) ?: return false
        val config = YamlConfiguration()
        config.set("distance", options.distance)
        config.set("round", options.round)
        config.set("rough", if (options.rough.isNaN()) "auto" else options.rough)
        config.set("step", options.step)
        file.parentFile.mkdirs()
        config.save(file)
        return true
    }

    fun defaultOptions() = TerrainOptions(
        defaults.defaultDistance, defaults.defaultRound, defaults.defaultRough, defaults.maxStep,
    )

    private fun fileFor(actor: Actor): File? {
        val player = actor as? BukkitPlayer ?: return null
        return File(File(plugin.dataFolder, "terrain-players"), "${player.uniqueId}.yml")
    }
}
