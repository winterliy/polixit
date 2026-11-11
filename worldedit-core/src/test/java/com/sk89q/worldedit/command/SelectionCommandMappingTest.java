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

package com.sk89q.worldedit.command;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.World;
import org.enginehub.piston.annotation.Command;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectionCommandMappingTest {

    @Test
    void pos1AndPos2AcceptOptionalSeparateCoordinates() throws ReflectiveOperationException {
        assertCoordinates("pos1", "/pos1");
        assertCoordinates("pos2", "/pos2");
    }

    private static void assertCoordinates(String methodName, String commandName)
            throws ReflectiveOperationException {
        Method method = Arrays.stream(SelectionCommands.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        assertEquals(commandName, method.getAnnotation(Command.class).name());

        assertArrayEquals(
                new Class<?>[]{Actor.class, World.class, LocalSession.class,
                        Integer.class, Integer.class, Integer.class},
                method.getParameterTypes());
    }
}
