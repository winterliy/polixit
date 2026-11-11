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

/** Water around a selection, read from its margin. */
object WaterInfo {
    /** Y of the top water block (median over the wet margin columns), or null when there is no water around. */
    fun levelOf(grid: SurfaceGrid): Int? {
        val m = grid.margin
        val levels = ArrayList<Int>()
        for (z in 0 until grid.depth) {
            for (x in 0 until grid.width) {
                if (x in m until grid.width - m && z in m until grid.depth - m) continue
                val i = z * grid.width + x
                if (grid.surface[i] > grid.heights[i]) levels += grid.surface[i]
            }
        }
        if (levels.isEmpty()) return null
        levels.sort()
        return levels[levels.size / 2]
    }
}
