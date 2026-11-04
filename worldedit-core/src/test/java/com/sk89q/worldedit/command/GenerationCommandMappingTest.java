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

import org.enginehub.piston.annotation.Command;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerationCommandMappingTest {

    @Test
    void formulaGenerationKeepsGenerateAndGButReleasesGen() throws ReflectiveOperationException {
        Command command = command("generate");

        assertEquals("/generate", command.name());
        assertArrayEquals(new String[]{"/g"}, command.aliases());
    }

    @Test
    void terrainGenerationOwnsGenAlias() throws ReflectiveOperationException {
        assertEquals("/gen", command("terrain").name());
    }

    private static Command command(String methodName) throws ReflectiveOperationException {
        Method method = Arrays.stream(GenerationCommands.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        return method.getAnnotation(Command.class);
    }
}
