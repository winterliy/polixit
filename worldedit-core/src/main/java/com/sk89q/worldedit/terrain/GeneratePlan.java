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

package com.sk89q.worldedit.terrain;

/**
 * Result of {@link TerrainGenerator#plan}.
 *
 * @param target         new surface Y per column of the selection plus {@code band} columns on
 *                       every side, row-major (z * (width + 2 * band) + x)
 * @param band           width of the transition band around the selection
 * @param zone           per column of {@code target}: true where it is the generating zone
 *                       (the selection minus its rounded-off corners), false in the band
 * @param roughness      undulation amplitude actually used
 * @param surroundings   statistics of the terrain around the selection
 * @param vanillaRelief  true when the detail came from vanilla-generated terrain, false for the noise fallback
 */
@SuppressWarnings("ArrayRecordComponent")
public record GeneratePlan(int[] target, int band, boolean[] zone, double roughness, TerrainStats surroundings, boolean vanillaRelief) {
}
