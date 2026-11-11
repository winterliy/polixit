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

package com.sk89q.worldedit.bukkit;

import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolixitPluginMetadataTest {

    @Test
    public void testPluginTargetsPolixitOnPaper263AndJava25() throws IOException {
        final String descriptor;
        try (InputStream input = getClass().getResourceAsStream("/plugin.yml")) {
            assertNotNull(input, "plugin.yml must be present in the built plugin resources");
            descriptor = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(descriptor.lines().anyMatch(line -> line.equals("name: Polixit")));
        assertTrue(descriptor.lines().anyMatch(line -> line.trim().equals("api-version: '26.3'")));
        assertTrue(descriptor.contains("org.jetbrains.kotlin:kotlin-stdlib:2.4.20"));

        final InputStream mainClass = getClass().getResourceAsStream("/com/sk89q/worldedit/bukkit/WorldEditPlugin.class");
        assertNotNull(mainClass, "main plugin class must be present");
        try (DataInputStream input = new DataInputStream(mainClass)) {
            assertEquals(0xCAFEBABE, input.readInt());
            input.readUnsignedShort();
            assertEquals(69, input.readUnsignedShort(), "main plugin class must target Java 25");
        }
    }
}
