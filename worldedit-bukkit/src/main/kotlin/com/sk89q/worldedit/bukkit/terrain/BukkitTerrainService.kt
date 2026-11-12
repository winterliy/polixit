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

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.LocalSession
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.blocks.BaseBlock
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.entity.Player
import com.sk89q.worldedit.extension.platform.TerrainService
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.terrain.GenerateParams
import com.sk89q.worldedit.terrain.TerrainStats
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

/** Bukkit implementation of Polixit's terrain-generation capability. */
class BukkitTerrainService(private val plugin: WorldEditPlugin) : TerrainService {

    private val settings: Settings by lazy {
        val file = File(plugin.dataFolder, "terrain.yml")
        if (!file.exists()) plugin.saveResource("terrain.yml", false)
        Settings.load(YamlConfiguration.loadConfiguration(file))
    }
    private val optionsStore: TerrainOptionsStore by lazy { TerrainOptionsStore(plugin, settings) }

    override fun generate(actor: Player, session: LocalSession, region: Region, level: Int?) {
        val regionWorld = region.world
        if (regionWorld == null) {
            actor.printError("The selection is not associated with a world.")
            return
        }
        if (region !is CuboidRegion) {
            actor.printError("Terrain generation currently requires a cuboid selection.")
            return
        }

        val world = try {
            (regionWorld as BukkitWorld).world ?: throw RuntimeException("unloaded")
        } catch (e: RuntimeException) {
            actor.printError("The selected world is not loaded.")
            return
        }
        val min = region.minimumPoint
        val max = region.maximumPoint
        val lowY = level ?: min.blockY
        val highY = level ?: max.blockY
        if (lowY !in LegacyCompat.minHeight(world) until world.maxHeight || highY !in LegacyCompat.minHeight(world) until world.maxHeight) {
            actor.printError("The requested surface height is outside this world's build range.")
            return
        }

        val width = region.width
        val depth = region.length
        val selectedColumns = width.toLong() * depth.toLong()
        val config = settings
        if (selectedColumns > config.maxColumns) {
            actor.printError("The selection exceeds the configured terrain-generation column limit.")
            return
        }

        val options = optionsStore.load(actor)
        val margin = options.distance + config.analysisMargin
        val originX = min.blockX - margin
        val originZ = min.blockZ - margin
        val gridWidth = width + margin * 2
        val gridDepth = depth + margin * 2
        val seed = world.seed
        val minHeight = LegacyCompat.minHeight(world)
        val maxHeight = world.maxHeight
        actor.print("Analyzing terrain around the selection...")

        val future: CompletableFuture<Generation> = SurfaceScanner.scan(
            world, min.blockX, min.blockZ, max.blockX, max.blockZ, margin, ForkJoinPool.commonPool(),
        ).thenApplyAsync({ grid ->
            // No version adapter can sample vanilla terrain before 1.13.
            val vanilla: VanillaColumns? = null
            val params = GenerateParams(
                lowY, highY, options.rough, config.scale, options.distance,
                2, options.step, seed, minHeight, maxHeight - 1, options.round,
            )
            Generation.design(
                grid, vanilla, params, config.maxFillDepth, config.copyPlants,
                config.beachWidth, config.fillOres,
            )
        }, ForkJoinPool.commonPool())

        future.whenComplete { generation, failure ->
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (failure != null) {
                    plugin.logger.log(java.util.logging.Level.WARNING, "Terrain planning failed", failure)
                    actor.printError("Terrain planning failed: ${failure.message ?: "unknown error"}")
                } else {
                    apply(actor, session, world, generation)
                }
            })
        }
    }

    override fun showInfo(actor: Player, region: Region) {
        val regionWorld = region.world
        if (regionWorld == null || region !is CuboidRegion) {
            actor.printError("Terrain analysis requires a cuboid selection in a loaded world.")
            return
        }
        val world = try {
            (regionWorld as BukkitWorld).world ?: throw RuntimeException("unloaded")
        } catch (e: RuntimeException) {
            actor.printError("The selected world is not loaded.")
            return
        }
        val min = region.minimumPoint
        val max = region.maximumPoint
        val margin = settings.analysisMargin.coerceAtLeast(1)
        actor.print("Analyzing surrounding terrain...")
        SurfaceScanner.scan(world, min.blockX, min.blockZ, max.blockX, max.blockZ, margin, ForkJoinPool.commonPool())
            .whenComplete { grid, failure ->
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    if (failure != null) {
                        actor.printError("Terrain analysis failed: ${failure.message ?: "unknown error"}")
                    } else {
                        val stats = TerrainStats.ofBand(grid.heights, grid.width, grid.depth, grid.margin)
                        actor.print(
                            "Surroundings: ${stats.describe()}, Y ${stats.min()}–${stats.max()}, " +
                                "median ${stats.median()}, roughness ${"%.2f".format(stats.roughness())}; " +
                                "selection Y ${min.blockY}–${max.blockY}."
                        )
                    }
                })
            }
    }

    override fun configure(actor: Player, setting: String?, value: String?) {
        val options = optionsStore.load(actor)
        if (setting == null) {
            printOptions(actor, options)
            return
        }
        val defaults = optionsStore.defaultOptions()
        val updated = when (setting.lowercase()) {
            "distance" -> {
                val parsed = value?.toIntOrNull()
                if (value == null) options.copy(distance = defaults.distance)
                else if (parsed == null || parsed !in 0..64) {
                    actor.printError("distance must be an integer from 0 to 64.")
                    return
                } else options.copy(distance = parsed)
            }
            "round" -> {
                val parsed = value?.toIntOrNull()
                if (value == null) options.copy(round = defaults.round)
                else if (parsed == null || parsed !in 0..100) {
                    actor.printError("round must be an integer from 0 to 100.")
                    return
                } else options.copy(round = parsed)
            }
            "rough" -> {
                if (value == null) options.copy(rough = defaults.rough)
                else {
                    val parsed = Settings.parseRough(value)
                    if (parsed == null || (!parsed.isNaN() && parsed > 16.0)) {
                        actor.printError("rough must be auto or a number from 0 to 16.")
                        return
                    }
                    options.copy(rough = parsed)
                }
            }
            "step" -> {
                val parsed = value?.toDoubleOrNull()
                if (value == null) options.copy(step = defaults.step)
                else if (parsed == null || parsed !in 0.0..16.0) {
                    actor.printError("step must be a number from 0 to 16.")
                    return
                } else options.copy(step = parsed)
            }
            else -> {
                actor.printError("Setting must be distance, round, rough, or step.")
                return
            }
        }
        if (!optionsStore.save(actor, updated)) {
            actor.printError("Per-player terrain settings require a player.")
            return
        }
        printOptions(actor, updated)
    }

    private fun printOptions(actor: Player, options: TerrainOptions) {
        val rough = if (options.rough.isNaN()) "auto" else options.rough.toString()
        actor.print(
            "Terrain settings: distance ${options.distance}, round ${options.round}, rough $rough, step ${options.step}."
        )
    }

    private fun apply(actor: Player, session: LocalSession, world: World, generation: Generation) {
        val changes = generation.changes
        actor.print("Applying ${changes.size} terrain block changes...")
        val editSession = session.createEditSession(actor)
        editSession.setFastMode(true)
        var cursor = 0
        lateinit var task: BukkitTask
        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            try {
                val end = minOf(cursor + settings.blocksPerTick, changes.size)
                changes.forEach(cursor until end, false) { x, y, z, data ->
                    @Suppress("DEPRECATION")
                    editSession.setBlock(Vector(x, y, z), BaseBlock(data.material.id, data.data))
                }
                cursor = end
                editSession.flushQueue()
                if (cursor >= changes.size) {
                    task.cancel()
                    editSession.flushQueue()
                    session.remember(editSession)
                    actor.print("Terrain generation complete: ${changes.size} blocks changed.")
                }
            } catch (failure: Throwable) {
                task.cancel()
                rollback(actor, session, editSession, failure)
            }
        }, 1L, 1L)
    }

    private fun rollback(actor: Player, session: LocalSession, editSession: EditSession, failure: Throwable) {
        try {
            editSession.flushQueue()
            val rollbackSession = session.createEditSession(actor)
            editSession.undo(rollbackSession)
            rollbackSession.flushQueue()
        } catch (rollbackFailure: Throwable) {
            failure.addSuppressed(rollbackFailure)
            plugin.logger.log(java.util.logging.Level.SEVERE, "Terrain rollback failed", failure)
        }
        actor.printError("Terrain generation failed; applied changes were rolled back.")
    }
}
