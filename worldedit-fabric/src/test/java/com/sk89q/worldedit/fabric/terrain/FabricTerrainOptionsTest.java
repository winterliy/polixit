package com.sk89q.worldedit.fabric.terrain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FabricTerrainOptionsTest {
    @TempDir Path tempDir;

    @Test void defaultsAndPersistenceRoundTrip() throws Exception {
        UUID player = UUID.randomUUID();
        assertEquals(FabricTerrainOptions.DEFAULT, FabricTerrainOptions.load(tempDir, player));
        FabricTerrainOptions options = new FabricTerrainOptions(20, 30, 2.5, 0.75);
        options.save(tempDir, player);
        assertEquals(options, FabricTerrainOptions.load(tempDir, player));
    }

    @Test void acceptsBoundaryValues() {
        assertDoesNotThrow(() -> new FabricTerrainOptions(0, 0, 0, 0));
        assertDoesNotThrow(() -> new FabricTerrainOptions(64, 100, 16, 16));
        assertTrue(Double.isNaN(FabricTerrainOptions.parseRough("auto")));
    }

    @Test void rejectsOutOfRangeValues() {
        assertThrows(IllegalArgumentException.class, () -> new FabricTerrainOptions(65, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new FabricTerrainOptions(0, 101, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new FabricTerrainOptions(0, 0, 17, 1));
        assertThrows(IllegalArgumentException.class, () -> new FabricTerrainOptions(0, 0, 0, 17));
        assertThrows(IllegalArgumentException.class, () -> FabricTerrainOptions.parseRough("NaN"));
    }
}
