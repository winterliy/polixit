/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sk89q.worldedit.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.TerrainService;
import com.sk89q.worldedit.regions.Region;

import javax.annotation.Nullable;

import static com.sk89q.minecraft.util.commands.Logging.LogMode.ALL;

/** Polixit terrain commands: //gen, //terrain info, //terrain config. */
public class TerrainCommands {

    private final WorldEdit worldEdit;

    public TerrainCommands(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    /** //gen [level]: registered at the top level. */
    public static class Gen {
        private final TerrainCommands commands;

        public Gen(WorldEdit worldEdit) {
            this.commands = new TerrainCommands(worldEdit);
        }

        @Command(aliases = { "/gen" }, usage = "[level]", desc = "Generates new terrain inside the selected region.", min = 0, max = 1)
        @CommandPermissions("worldedit.generation.terrain")
        @Logging(ALL)
        public void gen(Player player, LocalSession session, CommandContext args) throws IncompleteRegionException {
            TerrainService service = commands.terrainService(player);
            if (service == null) return;
            Region region = session.getSelection(player.getWorld());
            Integer level = null;
            if (args.argsLength() == 1) {
                try {
                    level = Integer.parseInt(args.getString(0));
                } catch (NumberFormatException e) {
                    player.printError("The level must be a whole number.");
                    return;
                }
            }
            service.generate(player, session, region, level);
        }
    }

    @Command(aliases = { "info" }, desc = "Analyze terrain around the selected region.", min = 0, max = 0)
    @CommandPermissions("worldedit.generation.terrain")
    public void info(Player player, LocalSession session) throws IncompleteRegionException {
        TerrainService service = terrainService(player);
        if (service != null) service.showInfo(player, session.getSelection(player.getWorld()));
    }

    @Command(aliases = { "config" }, usage = "[setting] [value]", desc = "Show or change terrain-generation settings.", min = 0, max = 2)
    @CommandPermissions("worldedit.generation.terrain")
    public void config(Player player, CommandContext args) {
        TerrainService service = terrainService(player);
        if (service != null) {
            service.configure(player, args.argsLength() > 0 ? args.getString(0) : null, args.argsLength() > 1 ? args.getString(1) : null);
        }
    }

    @Nullable
    TerrainService terrainService(Player player) {
        TerrainService service = worldEdit.getPlatformManager().queryCapability(Capability.WORLD_EDITING).getTerrainService();
        if (service == null) player.printError("Terrain generation is not supported by this platform.");
        return service;
    }
}
