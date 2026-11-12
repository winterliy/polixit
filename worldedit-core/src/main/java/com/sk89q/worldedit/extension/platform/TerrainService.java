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

package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.Region;

import javax.annotation.Nullable;

/** Polixit terrain generation, provided by platforms that can read and write the world (Bukkit). */
public interface TerrainService {

    /** Start terrain generation for the selected region; {@code level} fixes the surface Y, null uses the selection corners. */
    void generate(Player player, LocalSession session, Region region, @Nullable Integer level);

    /** Display terrain analysis around a selection. */
    void showInfo(Player player, Region region);

    /** Show or update the player's terrain options; a null value restores the default. */
    void configure(Player player, @Nullable String setting, @Nullable String value);
}
