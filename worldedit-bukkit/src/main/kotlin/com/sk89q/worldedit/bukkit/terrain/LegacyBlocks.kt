/*
 * Polixit terrain generation for WorldEdit 6 (Minecraft 1.12.2 / 1.8.8).
 * Copyright (C) winterliy and contributors. Licensed under the GNU GPL v3 or later.
 */

package com.sk89q.worldedit.bukkit.terrain

import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.World

/**
 * Before 1.13 a block is a material plus a 4-bit data value. The terrain code was written against 1.13's
 * BlockData, so this small value type stands in for it (equality = same material and data).
 */
data class BlockData(val material: Material, val data: Int = 0)

fun Material.createBlockData() = BlockData(this, 0)

/** The block at local chunk coordinates (ChunkSnapshot#getBlockData returns only the data nibble before 1.13). */
@Suppress("DEPRECATION")
fun ChunkSnapshot.blockAt(x: Int, y: Int, z: Int): BlockData = BlockData(typeAt(x, y, z), getBlockData(x, y, z))

@Suppress("DEPRECATION")
fun ChunkSnapshot.typeAt(x: Int, y: Int, z: Int): Material = Material.getMaterial(getBlockTypeId(x, y, z)) ?: Material.AIR

/** Bukkit API differences between 1.8/1.12 and newer versions. */
internal object LegacyCompat {
    /** First of the named materials this server has, or null. */
    fun material(vararg names: String): Material? = names.firstNotNullOfOrNull { Material.matchMaterial(it) }

    fun materialOr(vararg names: String): Material = material(*names) ?: error("none of ${names.toList()} exist")

    /** Worlds start at Y 0 before 1.17. */
    @Suppress("UNUSED_PARAMETER")
    fun minHeight(world: World): Int = 0

    fun isAir(material: Material): Boolean = material == Material.AIR

    val GRASS_BLOCK = materialOr("GRASS")
    val WATER = setOfNotNull(material("WATER"), material("STATIONARY_WATER"))
    val LAVA = setOfNotNull(material("LAVA"), material("STATIONARY_LAVA"))
    val LOGS = setOfNotNull(material("LOG"), material("LOG_2"))
    val LEAVES = setOfNotNull(material("LEAVES"), material("LEAVES_2"))
}
