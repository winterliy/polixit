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

package com.sk89q.worldedit.bukkit.terrain;

/**
 * Inputs to {@link TerrainGenerator}.
 *
 * @param lowY         lowest ground level inside the selection
 * @param highY        highest ground level inside the selection; equal to {@code lowY} means dead flat,
 *                     otherwise the ground rolls between the two
 * @param roughness    small undulation in blocks on top of the rolling level (height ranges only),
 *                     or {@code NaN} to derive it from the surroundings
 * @param scale        horizontal size of the undulations in blocks
 * @param edge         width of the transition band around the selection, outside it, in blocks
 * @param smoothPasses 3x3 blur passes applied to the selection's ground
 * @param maxStep      steepest allowed height difference between neighbouring columns
 *                     (thermal erosion); 0 disables it
 * @param seed         noise seed (use the world seed so neighbouring runs line up)
 * @param minY         lowest allowed surface Y
 * @param maxY         highest allowed surface Y
 * @param round        how rounded the selection's corners are, 0 (square) to 100 (corner radius
 *                     half the shorter side); the cut-off corners join the transition band
 */
public record GenerateParams(
        int lowY,
        int highY,
        double roughness,
        double scale,
        int edge,
        int smoothPasses,
        double maxStep,
        long seed,
        int minY,
        int maxY,
        int round) {

    public GenerateParams {
        if (scale <= 0) throw new IllegalArgumentException("scale must be > 0");
        if (edge < 0) throw new IllegalArgumentException("edge must be >= 0");
        if (smoothPasses < 0) throw new IllegalArgumentException("smoothPasses must be >= 0");
        if (maxStep < 0) throw new IllegalArgumentException("maxStep must be >= 0");
        if (!Double.isNaN(roughness) && roughness < 0) throw new IllegalArgumentException("roughness must be >= 0");
        if (minY > maxY) throw new IllegalArgumentException("minY must be <= maxY");
        if (highY < lowY) throw new IllegalArgumentException("highY must be >= lowY");
        if (round < 0 || round > 100) throw new IllegalArgumentException("round must be 0..100");
    }

    public GenerateParams(int lowY, int highY, double roughness, double scale, int edge, int smoothPasses,
                          double maxStep, long seed, int minY, int maxY) {
        this(lowY, highY, roughness, scale, edge, smoothPasses, maxStep, seed, minY, maxY, 0);
    }

    /** Corner radius in blocks for a selection of {@code w} by {@code d} columns. */
    public double cornerRadius(int w, int d) {
        return round / 100.0 * (Math.min(w, d) - 1) / 2.0;
    }

    public boolean isFlat() {
        return lowY == highY;
    }
}
