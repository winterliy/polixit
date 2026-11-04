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
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extension.platform.Actor
import com.sk89q.worldedit.extension.platform.TerrainService
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.util.SideEffectSet
import com.sk89q.worldedit.util.formatting.text.TextComponent
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

    override fun generate(actor: Actor, session: LocalSession, region: Region, level: Int?) {
        val regionWorld = region.world
        if (regionWorld == null) {
            actor.printError(TextComponent.of("The selection is not associated with a world."))
            return
        }
        if (region !is CuboidRegion) {
            actor.printError(TextComponent.of("Terrain generation currently requires a cuboid selection."))
            return
        }

        val world = try {
            BukkitAdapter.adapt(regionWorld)
        } catch (e: RuntimeException) {
            actor.printError(TextComponent.of("The selected world is not loaded."))
            return
        }
        val min = region.minimumPoint
        val max = region.maximumPoint
        val lowY = level ?: min.y()
        val highY = level ?: max.y()
        if (lowY !in world.minHeight until world.maxHeight || highY !in world.minHeight until world.maxHeight) {
            actor.printError(TextComponent.of("The requested surface height is outside this world's build range."))
            return
        }

        val width = region.width
        val depth = region.length
        val selectedColumns = width.toLong() * depth.toLong()
        val config = settings
        if (selectedColumns > config.maxColumns) {
            actor.printError(TextComponent.of("The selection exceeds the configured terrain-generation column limit."))
            return
        }

        val margin = config.defaultDistance + config.analysisMargin
        val originX = min.x() - margin
        val originZ = min.z() - margin
        val gridWidth = width + margin * 2
        val gridDepth = depth + margin * 2
        val seed = world.seed
        val minHeight = world.minHeight
        val maxHeight = world.maxHeight
        actor.printInfo(TextComponent.of("Analyzing terrain around the selection..."))

        val future: CompletableFuture<Generation> = SurfaceScanner.scan(
            world, min.x(), min.z(), max.x(), max.z(), margin, ForkJoinPool.commonPool(),
        ).thenApplyAsync({ grid ->
            val vanilla = if (config.vanillaTerrain) {
                VanillaTerrain.sample(world, originX, originZ, gridWidth, gridDepth)
            } else {
                null
            }
            val params = GenerateParams(
                lowY, highY, config.defaultRough, config.scale, config.defaultDistance,
                2, config.maxStep, seed, minHeight, maxHeight - 1, config.defaultRound,
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
                    actor.printError(TextComponent.of("Terrain planning failed: ${failure.message ?: "unknown error"}"))
                } else {
                    apply(actor, session, world, generation)
                }
            })
        }
    }

    private fun apply(actor: Actor, session: LocalSession, world: World, generation: Generation) {
        val changes = generation.changes
        actor.printInfo(TextComponent.of("Applying ${changes.size} terrain block changes..."))
        val editSession = session.createEditSession(actor, PackedBlockHistory())
        editSession.setSideEffectApplier(SideEffectSet.none())
        var cursor = 0
        lateinit var task: BukkitTask
        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            try {
                val end = minOf(cursor + settings.blocksPerTick, changes.size)
                changes.forEach(cursor until end, false) { x, y, z, data ->
                    editSession.setBlock(BlockVector3.at(x, y, z), BukkitAdapter.adapt(data))
                }
                cursor = end
                Operations.completeBlindly(editSession.commit())
                if (cursor >= changes.size) {
                    task.cancel()
                    editSession.close()
                    session.remember(editSession)
                    actor.printInfo(TextComponent.of("Terrain generation complete: ${changes.size} blocks changed."))
                }
            } catch (failure: Throwable) {
                task.cancel()
                rollback(actor, session, editSession, failure)
            }
        }, 1L, 1L)
    }

    private fun rollback(actor: Actor, session: LocalSession, editSession: EditSession, failure: Throwable) {
        try {
            editSession.close()
            session.createEditSession(actor).use { rollbackSession ->
                editSession.undo(rollbackSession)
            }
        } catch (rollbackFailure: Throwable) {
            failure.addSuppressed(rollbackFailure)
            plugin.logger.log(java.util.logging.Level.SEVERE, "Terrain rollback failed", failure)
        }
        actor.printError(TextComponent.of("Terrain generation failed; applied changes were rolled back."))
    }
}
