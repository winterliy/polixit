/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.terrain.VanillaColumns;
import org.bukkit.World;

import javax.annotation.Nullable;

/** Access to optional version-specific terrain capabilities. */
public final class BukkitTerrainAccess {

    private BukkitTerrainAccess() {
    }

    /**
     * Sample terrain with the active server adapter, or return null if unsupported.
     */
    @Nullable
    public static VanillaColumns sampleVanillaTerrain(
        World world, int originX, int originZ, int width, int depth
    ) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        return adapter == null ? null : adapter.sampleVanillaTerrain(world, originX, originZ, width, depth);
    }
}
