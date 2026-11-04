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

import com.sk89q.worldedit.bukkit.terrain.GenerateParams
import com.sk89q.worldedit.bukkit.terrain.GeneratePlan
import com.sk89q.worldedit.bukkit.terrain.TerrainGenerator

/** Everything one /mt gen run decided, ready to apply. */
data class Generation(
    val plan: GeneratePlan,
    val changes: TerrainChanges,
) {
    companion object {
        /**
         * Off the main thread: reads only snapshots. [params]' edge is the band
         * width; [grid]'s margin must be wider than that.
         */
        fun design(
            grid: SurfaceGrid, vanilla: VanillaColumns?, params: GenerateParams,
            maxFillDepth: Int, copyPlants: Boolean, beachWidth: Int, fillOres: Boolean,
        ): Generation {
            val waterLevel = WaterInfo.levelOf(grid)
            // Under water the band meets the real floor, just like on land: the zone is pinned,
            // so a deep sea nearby only lengthens the slope down to it instead of dragging the zone along.
            val plan = TerrainGenerator.plan(
                grid.heights, grid.width, grid.depth, grid.margin, grid.originX, grid.originZ, params, vanilla?.heights
            )
            val changes = GenerateJob.computeChanges(
                grid, plan, maxFillDepth, vanilla, copyPlants, waterLevel, beachWidth, params.seed(), fillOres,
            )
            return Generation(plan, changes)
        }
    }
}
