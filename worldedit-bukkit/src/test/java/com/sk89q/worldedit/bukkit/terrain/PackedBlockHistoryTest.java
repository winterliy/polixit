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

import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class PackedBlockHistoryTest {

    @Test
    void undoAndRedoKeepTheFirstBeforeAndLastAfterStateForEachPosition() {
        PackedBlockHistory history = new PackedBlockHistory();
        BlockVector3 position = BlockVector3.at(-120, -63, 240);
        BaseBlock original = mock(BaseBlock.class);
        BaseBlock intermediate = mock(BaseBlock.class);
        BaseBlock result = mock(BaseBlock.class);
        history.add(new BlockChange(position, original, intermediate));
        history.add(new BlockChange(position, intermediate, result));

        assertEquals(1, history.size());
        BlockChange undo = (BlockChange) history.backwardIterator().next();
        BlockChange redo = (BlockChange) history.forwardIterator().next();
        assertEquals(position, undo.position());
        assertSame(original, undo.current());
        assertEquals(position, redo.position());
        assertSame(result, redo.current());
    }

    @Test
    void recordsDistinctPackedCoordinates() {
        PackedBlockHistory history = new PackedBlockHistory();
        BaseBlock before = mock(BaseBlock.class);
        BaseBlock after = mock(BaseBlock.class);
        BlockVector3 first = BlockVector3.at(-30_000_000, -2048, 30_000_000);
        BlockVector3 second = BlockVector3.at(30_000_000, 2047, -30_000_000);
        history.add(new BlockChange(first, before, after));
        history.add(new BlockChange(second, after, before));

        Iterator<Change> changes = history.forwardIterator();
        assertEquals(2, history.size());
        assertEquals(first, ((BlockChange) changes.next()).position());
        assertEquals(second, ((BlockChange) changes.next()).position());
    }
}
