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

import java.util.UUID

/**
 * Compact list of block edits: packed positions plus palette indices for the
 * before/after states. Large flattens touch millions of blocks, so no per-block objects.
 */
class TerrainChanges(val worldId: UUID) {
    private val palette = ArrayList<BlockData>()
    private val paletteIndex = HashMap<BlockData, Int>()

    private var positions = LongArray(1024)
    private var before = IntArray(1024)
    private var after = IntArray(1024)
    var size = 0
        private set

    fun add(x: Int, y: Int, z: Int, old: BlockData, new: BlockData) {
        if (old == new) return
        if (size == positions.size) {
            val cap = size * 2
            positions = positions.copyOf(cap)
            before = before.copyOf(cap)
            after = after.copyOf(cap)
        }
        positions[size] = pack(x, y, z)
        before[size] = idOf(old)
        after[size] = idOf(new)
        size++
    }

    fun forEach(range: IntRange, undo: Boolean, action: (x: Int, y: Int, z: Int, data: BlockData) -> Unit) {
        val states = if (undo) before else after
        for (i in range) {
            val p = positions[i]
            action(unpackX(p), unpackY(p), unpackZ(p), palette[states[i]])
        }
    }

    private fun idOf(data: BlockData) = paletteIndex.getOrPut(data) { palette.add(data); palette.size - 1 }

    // Same layout as vanilla BlockPos.asLong: 26 bits X, 26 bits Z, 12 bits Y.
    private fun pack(x: Int, y: Int, z: Int): Long =
        ((x.toLong() and 0x3FFFFFF) shl 38) or ((z.toLong() and 0x3FFFFFF) shl 12) or (y.toLong() and 0xFFF)

    private fun unpackX(p: Long) = (p shr 38).toInt()
    private fun unpackY(p: Long) = (p shl 52 shr 52).toInt()
    private fun unpackZ(p: Long) = (p shl 26 shr 38).toInt()
}
