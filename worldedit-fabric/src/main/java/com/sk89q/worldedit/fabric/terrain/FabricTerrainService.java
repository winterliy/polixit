/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 * Copyright (C) 2026 jaehyun <ano701.main@gmail.com> (Motopoly-derived terrain code)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sk89q.worldedit.fabric.terrain;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.coremc.internal.CoreMcPlayer;
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.coremc.internal.CoreMcWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.TerrainService;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.terrain.GenerateParams;
import com.sk89q.worldedit.terrain.GeneratePlan;
import com.sk89q.worldedit.terrain.TerrainGenerator;
import com.sk89q.worldedit.terrain.TerrainStats;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import javax.annotation.Nullable;

/** Fabric implementation that captures live data on the server thread and applies bounded edits per tick. */
public final class FabricTerrainService implements TerrainService {
    private static final int MAX_COLUMNS = 16_384;
    private static final int ANALYSIS_MARGIN = 8;
    private static final int EDITS_PER_TICK = 2_000;
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    private final Queue<Pending> pending = new ConcurrentLinkedQueue<>();
    private final Queue<Pending> active = new ConcurrentLinkedQueue<>();

    public FabricTerrainService(CoreMcPlatform platform) { }

    public void onPlayerDisconnect(net.minecraft.server.level.ServerPlayer player) {
        UUID playerId = player.getUUID();
        pending.removeIf(job -> job.actor instanceof CoreMcPlayer owner && owner.getUniqueId().equals(playerId));
        List<Pending> disconnected = active.stream()
            .filter(job -> job.actor instanceof CoreMcPlayer owner && owner.getUniqueId().equals(playerId)).toList();
        disconnected.forEach(job -> {
            if (active.remove(job)) rollback(job, "Terrain generation was cancelled after disconnect.");
        });
    }

    @Override public void generate(Actor actor, LocalSession session, Region region, @Nullable Integer level) {
        Selection selected = validate(actor, session, region);
        if (selected == null) return;
        int lowY = level == null ? selected.min.y() : level;
        int highY = level == null ? selected.max.y() : level;
        if (lowY < selected.world.getMinY() || highY >= selected.world.getMaxY()) {
            error(actor, "Requested surface height is outside this world's build range."); return;
        }
        FabricTerrainOptions options = options(actor);
        int margin = options.distance() + ANALYSIS_MARGIN;
        if ((selected.width + margin * 2L) * (selected.depth + margin * 2L) > MAX_COLUMNS) {
            error(actor, "Selection and analysis area exceed the Fabric terrain limit of " + MAX_COLUMNS + " columns."); return;
        }
        int gridW = selected.width + margin * 2;
        int gridD = selected.depth + margin * 2;
        int originX = selected.min.x() - margin;
        int originZ = selected.min.z() - margin;
        int[] heights = captureHeights(selected.world, originX, originZ, gridW, gridD);
        int minHeight = selected.world.getMinY();
        int maxHeight = selected.world.getMaxY() - 1;
        long seed = selected.world.getSeed();
        actor.printInfo(TextComponent.of("Planning terrain changes..."));

        CompletableFuture<List<BlockChange>> planning = CompletableFuture.supplyAsync(() -> {
            GenerateParams params = new GenerateParams(lowY, highY, options.rough(), 48.0, options.distance(),
                2, options.step(), seed, minHeight, maxHeight, options.round());
            GeneratePlan plan = TerrainGenerator.plan(heights, gridW, gridD, margin, originX, originZ, params);
            return makeChanges(selected, plan, originX, originZ, heights, gridW);
        }, ForkJoinPool.commonPool());
        CompletableFuture<List<BlockChange>> completion = planning.handle((changes, failure) -> {
            if (failure != null) pending.add(Pending.failure(actor, failure));
            else pending.add(new Pending(actor, session, selected.editWorld, changes, null));
            return changes;
        });
        completion.exceptionally(failure -> null);
    }

    private List<BlockChange> makeChanges(Selection selection, GeneratePlan plan,
                                          int originX, int originZ, int[] heights, int gridW) {
        List<BlockChange> changes = new ArrayList<>();
        int bandW = selection.width + plan.band() * 2;
        int bandD = selection.depth + plan.band() * 2;
        if ((long) bandW * bandD > MAX_COLUMNS) throw new IllegalArgumentException("Terrain plan exceeds the safe output limit.");
        BlockState air = FabricAdapter.get().fromNativeBlockState(Blocks.AIR.defaultBlockState());
        BlockState surface = FabricAdapter.get().fromNativeBlockState(Blocks.GRASS_BLOCK.defaultBlockState());
        BlockState dirt = FabricAdapter.get().fromNativeBlockState(Blocks.DIRT.defaultBlockState());
        BlockState stone = FabricAdapter.get().fromNativeBlockState(Blocks.STONE.defaultBlockState());
        int oldMinX = selection.min.x();
        int oldMinZ = selection.min.z();
        for (int z = 0; z < bandD; z++) for (int x = 0; x < bandW; x++) {
            int wx = oldMinX - plan.band() + x;
            int wz = oldMinZ - plan.band() + z;
            int targetY = plan.target()[z * bandW + x];
            int sourceY = heights[(wz - originZ) * gridW + wx - originX];

            // The explicit /gen [y] argument is independent of the selection's vertical bounds.
            // TerrainGenerator already clamps target heights to the world's build range.
            int boundedTargetY = targetY;
            if (boundedTargetY == sourceY) continue;
            for (int y = Math.min(boundedTargetY, sourceY) + 1; y <= Math.max(boundedTargetY, sourceY); y++) {
                if (y < selection.world.getMinY() || y >= selection.world.getMaxY()) continue;
                BlockState state;
                if (y > boundedTargetY) state = air;
                else if (y == boundedTargetY) state = surface;
                else if (y >= boundedTargetY - 3) state = dirt;
                else state = stone;
                changes.add(new BlockChange(BlockVector3.at(wx, y, wz), state));
            }
        }
        return List.copyOf(changes);
    }

    @Override public void showInfo(Actor actor, Region region) {
        Selection selected = validate(actor, null, region);
        if (selected == null) return;
        int margin = ANALYSIS_MARGIN;
        int w = selected.width + margin * 2, d = selected.depth + margin * 2;
        if ((long) w * d > MAX_COLUMNS) { error(actor, "Selection and analysis area exceed the Fabric terrain limit of " + MAX_COLUMNS + " columns."); return; }
        int[] heights = captureHeights(selected.world, selected.min.x() - margin, selected.min.z() - margin, w, d);
        TerrainStats stats = TerrainStats.ofBand(heights, w, d, margin);
        actor.printInfo(TextComponent.of("Surroundings: " + stats.min() + "–" + stats.max() + " Y, median "
            + stats.median() + ", roughness " + String.format("%.2f", stats.roughness()) + ". Selection Y "
            + selected.min.y() + "–" + selected.max.y() + "."));
    }

    @Override public void configure(Actor actor, @Nullable String setting, @Nullable String value) {
        FabricTerrainOptions current = options(actor);
        if (setting == null) { printOptions(actor, current); return; }
        FabricTerrainOptions updated;
        try {
            updated = switch (setting.toLowerCase(Locale.ROOT)) {
                case "distance" -> new FabricTerrainOptions(parseInt(value, current.distance(), 0, 64), current.round(), current.rough(), current.step());
                case "round" -> new FabricTerrainOptions(current.distance(), parseInt(value, current.round(), 0, 100), current.rough(), current.step());
                case "rough" -> new FabricTerrainOptions(current.distance(), current.round(), value == null ? FabricTerrainOptions.DEFAULT.rough() : FabricTerrainOptions.parseRough(value), current.step());
                case "step" -> new FabricTerrainOptions(current.distance(), current.round(), current.rough(), value == null ? FabricTerrainOptions.DEFAULT.step() : parseDouble(value, 0, 16));
                default -> throw new IllegalArgumentException("Setting must be distance, round, rough, or step.");
            };
        } catch (RuntimeException exception) { error(actor, exception.getMessage()); return; }
        if (!(actor instanceof CoreMcPlayer player)) { error(actor, "Per-player terrain settings require a player."); return; }
        try { updated.save(CONFIG_DIR, player.getUniqueId()); }
        catch (IOException exception) { error(actor, "Could not save terrain settings: " + exception.getMessage()); return; }
        printOptions(actor, updated);
    }

    private int parseInt(@Nullable String text, int fallback, int min, int max) {
        if (text == null) return fallback;
        int value = Integer.parseInt(text);
        if (value < min || value > max) throw new IllegalArgumentException("Value must be from " + min + " to " + max + ".");
        return value;
    }

    private double parseDouble(String text, double min, double max) {
        double value = Double.parseDouble(text);
        if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException("Value must be from " + min + " to " + max + ".");
        return value;
    }

    private FabricTerrainOptions options(Actor actor) {
        if (!(actor instanceof CoreMcPlayer player)) return FabricTerrainOptions.DEFAULT;
        try { return FabricTerrainOptions.load(CONFIG_DIR, player.getUniqueId()); }
        catch (IOException exception) { return FabricTerrainOptions.DEFAULT; }
    }

    private void printOptions(Actor actor, FabricTerrainOptions options) {
        actor.printInfo(TextComponent.of("Terrain settings: distance " + options.distance() + ", round " + options.round()
            + ", rough " + (Double.isNaN(options.rough()) ? "auto" : options.rough()) + ", step " + options.step() + "."));
    }

    private Selection validate(Actor actor, @Nullable LocalSession session, Region region) {
        if (!(region instanceof CuboidRegion cuboid) || !(region.getWorld() instanceof CoreMcWorld world)) {
            error(actor, "Terrain operation requires a cuboid selection in a loaded world."); return null;
        }
        BlockVector3 min = region.getMinimumPoint(), max = region.getMaximumPoint();
        int width = max.x() - min.x() + 1, depth = max.z() - min.z() + 1;
        if ((long) width * depth > MAX_COLUMNS) { error(actor, "Selection exceeds the Fabric terrain limit of " + MAX_COLUMNS + " columns."); return null; }
        ServerLevel nativeWorld;
        try { nativeWorld = world.getWorld(); }
        catch (RuntimeException exception) { error(actor, "Selected world is not loaded."); return null; }
        com.sk89q.worldedit.world.World editWorld = session != null && session.hasWorldOverride()
            ? session.getWorldOverride()
            : WorldEdit.getInstance().getPlatformManager().getWorldForEditing(world);
        if (!(editWorld instanceof CoreMcWorld editCoreWorld) || editCoreWorld.getWorld() != nativeWorld) {
            error(actor, "The selected world is not available to the Fabric editing platform."); return null;
        }
        int margin = ANALYSIS_MARGIN + (session == null ? 0 : options(actor).distance());
        int scanMinX = min.x() - margin, scanMaxX = max.x() + margin;
        int scanMinZ = min.z() - margin, scanMaxZ = max.z() + margin;
        if (scanMinX < -30_000_000 || scanMaxX > 30_000_000 || scanMinZ < -30_000_000 || scanMaxZ > 30_000_000) {
            error(actor, "Selection is too close to the world border for terrain analysis."); return null;
        }
        for (int cx = scanMinX >> 4; cx <= scanMaxX >> 4; cx++)
            for (int cz = scanMinZ >> 4; cz <= scanMaxZ >> 4; cz++)
                if (!nativeWorld.getChunkSource().hasChunk(cx, cz)) { error(actor, "Terrain operation only scans already loaded chunks."); return null; }
        return new Selection(cuboid, world, nativeWorld, editWorld, min, max, width, depth);
    }

    private int[] captureHeights(ServerLevel world, int x0, int z0, int width, int depth) {
        if (width <= 0 || depth <= 0 || (long) width * depth > MAX_COLUMNS + 4L * MAX_COLUMNS) {
            throw new IllegalArgumentException("Terrain scan exceeds the safe capture limit.");
        }
        int[] heights = new int[width * depth];
        for (int z = 0; z < depth; z++) for (int x = 0; x < width; x++) {
            heights[z * width + x] = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x0 + x, z0 + z) - 1;
        }
        return heights;
    }

    /** Drain a bounded batch on the Fabric server thread. */
    public void tick(MinecraftServer server) {
        Pending job = active.peek();
        if (job == null) {
            job = pending.peek();
            if (job == null) return;
            if (job.failure != null) { pending.poll(); error(job.actor, "Terrain planning failed: " + rootMessage(job.failure)); return; }
            if (job.session == null) { pending.poll(); return; }
        }
        if (job.editSession == null) {
            pending.remove(job);
            job.editSession = job.session.createEditSession(job.actor);
            if (job.editSession.getWorld() == null || !job.editSession.getWorld().equals(job.world)) {
                error(job.actor, "The active WorldEdit session targets a different world; terrain edits were cancelled.");
                job.editSession.close(); active.remove(job); return;
            }
            job.editSession.setSideEffectApplier(SideEffectSet.none());
            active.add(job);
        }
        try {
            int end = Math.min(job.cursor + EDITS_PER_TICK, job.changes.size());
            while (job.cursor < end) {
                BlockChange change = job.changes.get(job.cursor++);
                job.editSession.setBlock(change.position, change.state);
            }
            job.editSession.commit();
            if (job.cursor >= job.changes.size()) {
                active.remove(job);
                job.editSession.close(); job.session.remember(job.editSession);
                job.actor.printInfo(TextComponent.of("Terrain generation complete: " + job.changes.size() + " blocks changed."));
            }
        } catch (Throwable failure) {
            rollback(job, "Terrain generation failed and applied edits were rolled back.");
        }
    }

    /** Cancel queued work during shutdown; incomplete edits are rolled back before the server stops. */
    public void shutdown() {
        Pending job;
        while ((job = pending.poll()) != null) {
            if (job.failure == null && job.session != null) error(job.actor, "Terrain generation was cancelled during server shutdown.");
        }
        List<Pending> incomplete = List.copyOf(active);
        incomplete.forEach(pendingJob -> {
            if (active.remove(pendingJob)) rollback(pendingJob, "Incomplete terrain generation was rolled back during server shutdown.");
        });
    }

    private void rollback(Pending job, String message) {
        try {
            job.editSession.close();
            try (EditSession rollback = job.session.createEditSession(job.actor)) {
                rollback.setBlockChangeLimit(-1);
                job.editSession.undo(rollback); rollback.commit();
            }
            error(job.actor, message);
        } catch (Throwable failure) {
            error(job.actor, "Could not roll back incomplete terrain generation: " + rootMessage(failure));
        }
    }

    private static String rootMessage(Throwable failure) {
        Throwable root = failure;
        while (root.getCause() != null) root = root.getCause();
        return root.getMessage() == null ? "unknown error" : root.getMessage();
    }

    private void error(Actor actor, String message) { actor.printError(TextComponent.of(message == null ? "Invalid terrain setting." : message)); }

    private record BlockChange(BlockVector3 position, BlockState state) { }
    private record Selection(CuboidRegion region, CoreMcWorld worldEditWorld, ServerLevel world,
                             com.sk89q.worldedit.world.World editWorld, BlockVector3 min, BlockVector3 max, int width, int depth) { }
    private static final class Pending {
        final Actor actor; final LocalSession session;
        final com.sk89q.worldedit.world.World world;
        final List<BlockChange> changes; final Throwable failure;
        int cursor; EditSession editSession;
        Pending(Actor actor, LocalSession session, com.sk89q.worldedit.world.World world, List<BlockChange> changes, Throwable failure) {
            this.actor = actor; this.session = session; this.world = world; this.changes = changes; this.failure = failure;
        }
        static Pending failure(Actor actor, Throwable failure) { return new Pending(actor, null, null, List.of(), failure); }
    }
}
