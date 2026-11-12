/*
 * Polixit terrain generation for WorldEdit 6 (Minecraft 1.12.2 / 1.8.8).
 * Copyright (C) winterliy and contributors. Licensed under the GNU GPL v3 or later.
 */

package com.sk89q.worldedit.bukkit.terrain

import com.sk89q.worldedit.terrain.OctaveNoise
import org.bukkit.Material

/**
 * Ground cover for new land on 1.8/1.12: grass and ferns, small flowers and the odd tall plant on soil, dead bushes on
 * sand. Two slow noises make meadows, bare patches and flower clumps; everything depends only on position and seed.
 */
class Vegetation(private val seed: Long) {
    private val patches = OctaveNoise(seed xor PATCH_SALT, 2)
    private val clumps = OctaveNoise(seed xor CLUMP_SALT, 2)

    /** Plants to stand on [ground] at (x, z), bottom first; empty for bare ground. The biome is unknown here. */
    @Suppress("UNUSED_PARAMETER")
    fun plantsAt(biome: String, ground: Material, x: Int, z: Int): List<BlockData> {
        val density = (1.0 + PATCHINESS * patches.sample(x / PATCH_SCALE, z / PATCH_SCALE)).coerceAtLeast(0.0)
        var r = unit(hash(seed, x, z))
        for (plant in COVER) {
            if (ground !in plant.soil) continue
            val chance = plant.chance * density
            if (r < chance) return plant.pick(clumps.sample(x / CLUMP_SCALE, z / CLUMP_SCALE), hash(seed + 1, x, z))
            r -= chance
        }
        return emptyList()
    }

    private class Plant(val chance: Double, val soil: Set<Material>, val kinds: List<List<BlockData>>) {
        fun pick(clump: Double, h: Long): List<BlockData> {
            val base = ((clump + 1) / 2 * kinds.size).toInt().coerceIn(0, kinds.size - 1)
            val k = if (unit(h ushr 7) < 0.2) (base + 1) % kinds.size else base
            return kinds[k]
        }
    }

    private companion object {
        const val PATCH_SALT = 0x1B873593L
        const val CLUMP_SALT = 0x5BD1E995L
        const val PATCH_SCALE = 18.0
        const val CLUMP_SCALE = 9.0
        const val PATCHINESS = 0.9

        val SOIL = setOfNotNull(LegacyCompat.GRASS_BLOCK, Material.DIRT)
        val SAND = setOf(Material.SAND)
        private val TALL_GRASS = LegacyCompat.materialOr("LONG_GRASS")
        private val DOUBLE_PLANT = LegacyCompat.materialOr("DOUBLE_PLANT")
        private val DANDELION = LegacyCompat.materialOr("YELLOW_FLOWER")
        private val FLOWER = LegacyCompat.materialOr("RED_ROSE")

        fun one(m: Material, data: Int = 0) = listOf(BlockData(m, data))
        // Double plants: the lower half carries the variant, the upper half has bit 8 set.
        fun tall(variant: Int) = listOf(BlockData(DOUBLE_PLANT, variant), BlockData(DOUBLE_PLANT, 8))

        val COVER = listOf(
            Plant(0.22, SOIL, listOf(one(TALL_GRASS, 1))),
            Plant(0.03, SOIL, listOf(one(TALL_GRASS, 2), tall(2))),
            Plant(0.015, SOIL, listOf(one(DANDELION), one(FLOWER, 0), one(FLOWER, 3), one(FLOWER, 8), one(FLOWER, 4))),
            Plant(0.008, SAND, listOf(one(Material.DEAD_BUSH))),
        )

        fun hash(seed: Long, x: Int, z: Int): Long {
            var h = seed xor (x.toLong() * -0x61c8864680b583ebL) xor (z.toLong() * -0x3d4d51c2d82b14b1L)
            h = (h xor (h ushr 33)) * -0xae502812aa7333L
            h = (h xor (h ushr 33)) * -0x3b314601e57a13adL
            return h xor (h ushr 33)
        }

        fun unit(h: Long) = (h ushr 11).toDouble() / (1L shl 53)
    }
}
