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

import java.util.Random;

/**
 * Fractal sum of {@link ImprovedNoise} octaves, like Minecraft's {@code PerlinNoise}:
 * each octave doubles the frequency and halves the amplitude. The result is
 * normalised by the total amplitude so it stays roughly in [-1, 1].
 */
public final class OctaveNoise {
    private final ImprovedNoise[] octaves;
    private final double amplitudeSum;

    public OctaveNoise(long seed, int octaveCount) {
        if (octaveCount < 1) {
            throw new IllegalArgumentException("octaveCount must be >= 1");
        }
        Random random = new Random(seed);
        octaves = new ImprovedNoise[octaveCount];
        double sum = 0;
        for (int i = 0; i < octaveCount; i++) {
            octaves[i] = new ImprovedNoise(random);
            sum += 1.0 / (1 << i);
        }
        amplitudeSum = sum;
    }

    public double sample(double x, double z) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        for (ImprovedNoise octave : octaves) {
            total += octave.noise(x * frequency, 0, z * frequency) * amplitude;
            frequency *= 2;
            amplitude *= 0.5;
        }
        return total / amplitudeSum;
    }
}
