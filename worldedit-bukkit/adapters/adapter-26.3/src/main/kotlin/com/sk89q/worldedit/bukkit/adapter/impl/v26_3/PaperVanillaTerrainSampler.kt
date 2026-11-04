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

package com.sk89q.worldedit.bukkit.adapter.impl.v26_3

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ProtoChunk
import net.minecraft.world.level.chunk.UpgradeData
import net.minecraft.world.level.levelgen.Aquifer
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import net.minecraft.world.level.levelgen.NoiseChunk
import net.minecraft.world.level.levelgen.NoiseSettings
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume
import net.minecraft.world.level.levelgen.material.rule.MaterialRule
import org.bukkit.World
import org.bukkit.block.data.BlockData
import com.sk89q.worldedit.bukkit.terrain.VanillaColumns
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.util.function.Supplier
import java.util.stream.IntStream

/**
 * Runs vanilla's own terrain steps for every chunk under the grid, in
 * throwaway chunks that never reach the world: biomes, the noise fill
 * (stone, water, aquifers) and the surface rules (grass, sand, terracotta
 * bands, gravel and coarse dirt patches…). Biomes are read through vanilla's
 * fuzzy zoom, so borders wander exactly as they do in generated terrain.
 * Structures and carvers are left out: they would need the real world's chunks.
 */
object PaperVanillaTerrainSampler {
    const val STACK_DEPTH = 8
    private val logger = LoggerFactory.getLogger(PaperVanillaTerrainSampler::class.java)

    // Steps the generator keeps private; looked up once.
    private val chunkVolume: Method by lazy {
        NoiseBasedChunkGenerator::class.java
            .getDeclaredMethod("chunkVolume", ChunkAccess::class.java, NoiseSettings::class.java).apply { isAccessible = true }
    }
    private val doFill: Method by lazy {
        NoiseBasedChunkGenerator::class.java
            .getDeclaredMethod("doFill", NoiseChunk::class.java, ChunkAccess::class.java).apply { isAccessible = true }
    }
    private val buildSurface: Method by lazy {
        NoiseBasedChunkGenerator::class.java.getDeclaredMethod(
            "buildSurface", ChunkAccess::class.java, NoiseChunk::class.java, RandomState::class.java,
            BiomeManager::class.java, Set::class.java, MaterialRule::class.java, ServerLevel::class.java,
        ).apply { isAccessible = true }
    }
    private val fluidPicker by lazy {
        NoiseBasedChunkGenerator::class.java.getDeclaredField("globalFluidPicker").apply { isAccessible = true }
    }

    /**
     * Samples the columns [originX]..+[width], [originZ]..+[depth] of [world].
     * Safe off the main thread; chunks are spread over the common pool. Null
     * if the world doesn't use vanilla's noise generator or a step fails (logged).
     */
    fun sample(world: World, originX: Int, originZ: Int, width: Int, depth: Int): VanillaColumns? = try {
        val level = (world as CraftWorld).handle
        val generator = level.chunkSource.generator as? NoiseBasedChunkGenerator
            ?: error("${world.name} does not use vanilla's noise generator")
        val random = level.chunkSource.randomState()
        val settings = generator.generatorSettings().value()
        val sea = generator.seaLevel
        @Suppress("UNCHECKED_CAST")
        val fluids = (fluidPicker.get(generator) as Supplier<Aquifer.FluidPicker>).get()
        val possibleBiomes = generator.biomeSource.possibleBiomes()

        val n = width * depth
        val heights = IntArray(n)
        val stacks = arrayOfNulls<Array<BlockData>>(n)
        val biomes = arrayOfNulls<String>(n)
        val snowy = BooleanArray(n)
        val minCx = originX shr 4
        val minCz = originZ shr 4
        val chunksX = ((originX + width - 1) shr 4) - minCx + 1
        val chunksZ = ((originZ + depth - 1) shr 4) - minCz + 1

        IntStream.range(0, chunksX * chunksZ).parallel().forEach { c ->
            val pos = ChunkPos(minCx + c % chunksX, minCz + c / chunksX)
            val chunk = ProtoChunk(pos, UpgradeData.EMPTY, level, level.palettedContainerFactory(), null)
            generator.createBiomes(random, Blender.empty(), level.structureManager(), chunk).join()
            // Same zoom as a generation region: fuzzy borders over the noise biomes, which reach past the chunk edge.
            val biomeManager = BiomeManager(generator.biomeSource.createUncachedResolver(random), BiomeManager.obfuscateSeed(level.seed))
            val noiseSettings = settings.noiseSettings().clampToHeightAccessor(chunk.heightAccessorForGeneration)
            val volume = chunkVolume.invoke(null, chunk, noiseSettings) as DensityVolume
            NoiseChunk(random, null, settings, fluids, Blender.empty(), volume).use { noise ->
                doFill.invoke(generator, noise, chunk)
                buildSurface.invoke(generator, chunk, noise, random, biomeManager, possibleBiomes, settings.materialRule().value(), level)
            }

            val surfacePos = BlockPos.MutableBlockPos()
            for (lz in 0 until 16) {
                val z = pos.minBlockZ + lz - originZ
                if (z !in 0 until depth) continue
                for (lx in 0 until 16) {
                    val x = pos.minBlockX + lx - originX
                    if (x !in 0 until width) continue
                    val i = z * width + x
                    val wx = pos.minBlockX + lx
                    val wz = pos.minBlockZ + lz
                    val top = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, lx, lz)
                    heights[i] = top
                    stacks[i] = Array(STACK_DEPTH) { d -> CraftBlockData.createData(chunk.getBlockState(wx, top - d, wz)) }
                    surfacePos.set(wx, top + 1, wz)
                    val biome = biomeManager.getBiome(surfacePos)
                    biomes[i] = biome.registeredName
                    snowy[i] = biome.value().coldEnoughToSnow(surfacePos, sea)
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        VanillaColumns(heights, stacks as Array<Array<BlockData>>, biomes as Array<String>, snowy, sea)
    } catch (e: Throwable) {
        logger.warn("Could not sample vanilla terrain in {}; falling back to noise", world.name, e)
        null
    }
}
