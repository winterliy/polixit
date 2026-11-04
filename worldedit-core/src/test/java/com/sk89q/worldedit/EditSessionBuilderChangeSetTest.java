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

package com.sk89q.worldedit;

import com.sk89q.worldedit.history.changeset.ArrayListHistory;
import com.sk89q.worldedit.history.changeset.BlockOptimizedHistory;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class EditSessionBuilderChangeSetTest extends BaseWorldEditTest {

    @Test
    void builtSessionKeepsTheDefaultHistoryWhenNoneWasProvided() {
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().build()) {
            org.junit.jupiter.api.Assertions.assertInstanceOf(BlockOptimizedHistory.class, session.getChangeSet());
        }
    }

    @Test
    void builtSessionUsesTheProvidedChangeSet() {
        ChangeSet changeSet = new ArrayListHistory();
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .changeSet(changeSet)
                .build()) {
            assertSame(changeSet, session.getChangeSet());
        }
    }
}
