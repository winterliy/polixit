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

package com.sk89q.worldedit.bukkit.terrain;

import com.google.common.collect.Iterators;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ArrayListHistory;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A compact WorldEdit history for block edits, with non-block changes delegated
 * to {@link ArrayListHistory}.
 *
 * <p>Positions use Minecraft's packed block-position layout. Block data is
 * stored once in a palette and referenced by integer indexes.</p>
 */
public final class PackedBlockHistory extends ArrayListHistory {

    private static final int INITIAL_CAPACITY = 1024;

    private final ArrayList<BaseBlock> palette = new ArrayList<>();
    private final Map<BaseBlock, Integer> paletteIndexes = new HashMap<>();
    private final Long2IntOpenHashMap positionIndexes = new Long2IntOpenHashMap(INITIAL_CAPACITY);
    private long[] positions = new long[INITIAL_CAPACITY];
    private int[] previous = new int[INITIAL_CAPACITY];
    private int[] current = new int[INITIAL_CAPACITY];
    private int blockCount;

    public PackedBlockHistory() {
        positionIndexes.defaultReturnValue(-1);
    }

    @Override
    public void add(Change change) {
        checkNotNull(change);
        if (!isRecordingChanges()) {
            return;
        }
        if (!(change instanceof BlockChange blockChange)) {
            super.add(change);
            return;
        }

        BlockVector3 position = blockChange.position();
        BlockVector3.checkLongPackable(position);
        long packedPosition = pack(position);
        int index = positionIndexes.get(packedPosition);
        if (index < 0) {
            ensureCapacity();
            index = blockCount++;
            positions[index] = packedPosition;
            previous[index] = paletteIndex(blockChange.previous());
            positionIndexes.put(packedPosition, index);
        }
        current[index] = paletteIndex(blockChange.current());
    }

    @Override
    public Iterator<Change> forwardIterator() {
        return Iterators.concat(super.forwardIterator(), blockIterator(false));
    }

    @Override
    public Iterator<Change> backwardIterator() {
        return Iterators.concat(super.backwardIterator(), blockIterator(true));
    }

    @Override
    public int size() {
        return super.size() + blockCount;
    }

    private Iterator<Change> blockIterator(boolean undo) {
        return new Iterator<>() {
            private int cursor = undo ? blockCount - 1 : 0;

            @Override
            public boolean hasNext() {
                return undo ? cursor >= 0 : cursor < blockCount;
            }

            @Override
            public Change next() {
                int index = cursor;
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                cursor += undo ? -1 : 1;
                BaseBlock block = palette.get(undo ? previous[index] : current[index]);
                return new BlockChange(BlockVector3.fromLongPackedForm(positions[index]), block, block);
            }
        };
    }

    private int paletteIndex(BaseBlock block) {
        return paletteIndexes.computeIfAbsent(block, value -> {
            palette.add(value);
            return palette.size() - 1;
        });
    }

    private void ensureCapacity() {
        if (blockCount < positions.length) {
            return;
        }
        int newCapacity = positions.length * 2;
        positions = java.util.Arrays.copyOf(positions, newCapacity);
        previous = java.util.Arrays.copyOf(previous, newCapacity);
        current = java.util.Arrays.copyOf(current, newCapacity);
    }

    private static long pack(BlockVector3 position) {
        return ((long) position.x() & 0x3FFFFFFL)
                | (((long) position.z() & 0x3FFFFFFL) << 26)
                | (((long) position.y() & 0xFFFL) << 52);
    }
}
