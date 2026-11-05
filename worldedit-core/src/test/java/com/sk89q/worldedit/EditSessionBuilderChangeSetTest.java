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
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class EditSessionBuilderChangeSetTest extends BaseWorldEditTest {

    @Test
    void builtSessionKeepsTheDefaultHistoryWhenNoneWasProvided() {
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().build()) {
            org.junit.jupiter.api.Assertions.assertInstanceOf(BlockOptimizedHistory.class, session.getChangeSet());
        }
    }

    @Test
    void worldBackedSessionUsesItsDefaultHistoryExtent() {
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(mock(World.class))
                .build()) {
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

    @Test
    void localSessionCanCreateAnEditSessionWithCustomHistory() {
        ChangeSet changeSet = new ArrayListHistory();
        Actor actor = mock(Actor.class);
        try (EditSession editSession = new LocalSession().createEditSession(actor, changeSet)) {
            assertSame(changeSet, editSession.getChangeSet());
        }
    }
}
