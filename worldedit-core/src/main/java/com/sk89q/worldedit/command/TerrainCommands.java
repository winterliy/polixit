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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.TerrainService;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

import javax.annotation.Nullable;

/** Commands for terrain analysis and per-player generation settings. */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public final class TerrainCommands {

    private final WorldEdit worldEdit;

    public TerrainCommands(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    @Command(name = "info", desc = "Analyze terrain around the selected region.")
    @CommandPermissions("worldedit.generation.terrain")
    public void info(Actor actor, @Selection Region region) {
        TerrainService service = terrainService(actor);
        if (service != null) {
            service.showInfo(actor, region);
        }
    }

    @Command(name = "config", desc = "Show or change terrain-generation settings.")
    @CommandPermissions("worldedit.generation.terrain")
    public void config(Actor actor,
                       @Arg(name = "setting", desc = "distance, round, rough, or step", def = "") String setting,
                       @Arg(name = "value", desc = "New value; omit to restore the default", def = "") String value) {
        TerrainService service = terrainService(actor);
        if (service != null) {
            service.configure(actor, emptyToNull(setting), emptyToNull(value));
        }
    }

    @Nullable
    private TerrainService terrainService(Actor actor) {
        TerrainService service = worldEdit.getPlatformManager()
            .queryCapability(Capability.WORLD_EDITING).getTerrainService();
        if (service == null) {
            actor.printError(TextComponent.of("Terrain generation is not supported by this platform."));
        }
        return service;
    }

    @Nullable
    private static String emptyToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
