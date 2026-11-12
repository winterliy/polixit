/*
 * Polixit, a Minecraft terrain and world editing plugin
 * Copyright (C) winterliy and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sk89q.worldedit.bukkit.terrain

import org.bukkit.Material
import org.bukkit.World

/** Bukkit API that differs between 1.13 and the newer versions the terrain code was written for. */
internal object LegacyCompat {
    /** First of the named materials this server has, or null. */
    fun material(vararg names: String): Material? = names.firstNotNullOfOrNull { Material.matchMaterial(it) }

    /** Like [material] but never null (the last name must exist on 1.13). */
    fun materialOr(vararg names: String): Material = material(*names) ?: error("none of ${names.toList()} exist")

    /** World#getMinHeight arrived in 1.17; worlds start at Y 0 before that. */
    fun minHeight(world: World): Int = try { world.minHeight } catch (_: NoSuchMethodError) { 0 }

    /** Material#isAir arrived in 1.14. */
    fun isAir(material: Material): Boolean = material.name.endsWith("AIR")
}
