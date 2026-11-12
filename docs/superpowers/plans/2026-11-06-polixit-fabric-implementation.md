# Polixit Fabric Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Add Polixit terrain generation to the Fabric 26.3 mod with WorldEdit selection, settings, and undo/redo.

**Architecture:** Move pure terrain planning algorithms from the Bukkit module into `worldedit-core`, then implement Fabric world capture and application in `worldedit-fabric`. Capture native world data on the server thread, plan from immutable arrays asynchronously, and apply bounded batches through `EditSession` so `LocalSession` records undo/redo.

**Tech Stack:** Java, Kotlin, Gradle, Fabric Loader/API, Minecraft 26.3, WorldEdit core, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-11-06-polixit-fabric-design.md`

## Global Constraints

- Target Minecraft/Fabric 26.3.
- Share platform-neutral terrain algorithms from `worldedit-core`.
- Never access live `ServerLevel` data off the server thread.
- Apply terrain changes in bounded Fabric server-tick batches through `EditSession` and `LocalSession`.
- Persist Fabric player settings by UUID under Fabric's config directory.
- Do not change Bukkit/Paper runtime behavior.
- Package the Fabric mod as Polixit and preserve GPLv3 and Motopoly MIT notices.

## Review Focus

- Oversized selections: reject before allocating arrays or loading additional chunks; test the configured column cap.
- World height boundaries: reject Y values outside the target world's build range; test minimum and maximum bounds.
- Partial `//pos1` or `//pos2` coordinates: keep the existing all-or-none validation; preserve the shared command mapping test.
- Invalid terrain settings: reject out-of-range values without overwriting a player's saved settings; test each accepted setting bound.
- Server shutdown during a queued edit: cancel or roll back an incomplete batch without retaining an undo entry; verify with a Fabric server smoke test if practical.

---

### Task 1: Share the pure terrain algorithms

**Files:**
- Create: `worldedit-core/src/main/java/com/sk89q/worldedit/terrain/{GenerateParams,GeneratePlan,ImprovedNoise,OctaveNoise,OreTable,TerrainGenerator,TerrainStats}.java`
- Modify: `worldedit-bukkit/src/main/kotlin/com/sk89q/worldedit/bukkit/terrain/{BukkitTerrainService,Generation,GenerateJob}.kt`
- Move tests: `worldedit-bukkit/src/test/java/com/sk89q/worldedit/bukkit/terrain/{TerrainGeneratorTest,OreTableTest}.java` to `worldedit-core/src/test/java/com/sk89q/worldedit/terrain/`

**Interfaces:**
- Produces shared package `com.sk89q.worldedit.terrain` for all platform-neutral algorithm classes.
- Bukkit Kotlin orchestration imports the shared package; no behavior or parameter defaults change.

- [x] Move the two algorithm tests and update their package/imports; run them and record the expected compile failure until production classes are moved.
- [ ] Move the seven pure Java algorithm classes to `worldedit-core` and update Bukkit imports.
- [ ] Run `./gradlew :worldedit-core:test :worldedit-bukkit:test` and confirm the existing algorithm assertions pass unchanged.
- [ ] Commit and push `refactor: share terrain algorithms across platforms` with required co-author trailers.

### Task 2: Implement Fabric terrain capture and planning

**Files:**
- Create: `worldedit-fabric/src/main/java/com/sk89q/worldedit/fabric/terrain/FabricTerrainService.java`
- Create: `worldedit-fabric/src/main/java/com/sk89q/worldedit/fabric/terrain/FabricTerrainOptions.java`
- Test: `worldedit-fabric/src/test/java/com/sk89q/worldedit/fabric/terrain/FabricTerrainOptionsTest.java`

**Interfaces:**
- `FabricTerrainService implements TerrainService` with `generate(Actor, LocalSession, Region, Integer)`, `showInfo(Actor, Region)`, `configure(Actor, String, String)`, and `tick(MinecraftServer)`.
- `FabricTerrainOptions` carries `distance`, `round`, `rough`, and `step`; options are saved by `CoreMcPlayer.getUniqueId()`.
- Captured terrain data contains only immutable height/material values; async planning consumes no live Minecraft world objects.

- [ ] Add failing tests for Fabric option defaults, accepted bounds, and invalid-value rejection.
- [ ] Implement per-player Fabric option loading/saving under `FabricLoader.getConfigDir()/polixit/terrain-players/`.
- [ ] Implement cuboid/loaded-world/Y/column-limit validation and server-thread terrain capture via `CoreMcWorld.getWorld()`.
- [ ] Plan with shared `TerrainGenerator` on a worker executor and queue immutable block edits for `tick(MinecraftServer)`.
- [ ] Implement `/terrain info` statistics and setting validation from the captured heights.
- [ ] Run Fabric tests and `./gradlew :worldedit-fabric:compileJava`.
- [ ] Commit and push `feat: add Fabric terrain service` with required co-author trailers.

### Task 3: Wire Fabric commands, metadata, and history application

**Files:**
- Modify: `worldedit-fabric/src/main/java/com/sk89q/worldedit/fabric/internal/FabricPlatform.java`
- Modify: `worldedit-fabric/src/main/java/com/sk89q/worldedit/fabric/internal/FabricWorldEdit.java`
- Modify: `worldedit-fabric/src/main/resources/fabric.mod.json`
- Modify: `worldedit-fabric/build.gradle.kts`

**Interfaces:**
- `FabricPlatform.getTerrainService()` returns the Fabric service.
- Fabric `END_SERVER_TICK` drains the service's bounded batch queue.
- Mod id and display name are `polixit`; internal Java packages remain `com.sk89q.worldedit.*`.

- [ ] Add a Fabric integration test or generated-registration assertion that terrain commands are reachable through the existing shared command registry.
- [ ] Wire the service into `FabricPlatform` and the server tick callback.
- [ ] Apply batches with `LocalSession.createEditSession(actor)`, remember only on completion, and roll back incomplete edits on failure/shutdown.
- [ ] Rename Fabric metadata and entrypoint lookup to Polixit; set the Fabric archive name to `polixit-fabric-mc26.3`.
- [ ] Run `./gradlew :worldedit-fabric:test :worldedit-fabric:shadowJar` and inspect the mod metadata and bundled license notices.
- [ ] Commit and push `feat: package Polixit terrain for Fabric` with required co-author trailers.

### Task 4: Validate a fresh Fabric 26.3 server and publish

**Files:**
- Modify: `README.md`
- Modify: `CHANGELOG.txt`
- Modify: `gradle.properties`

**Interfaces:**
- Release artifact is `worldedit-fabric/build/libs/polixit-fabric-mc26.3-<version>-dist.jar`.
- Release source archive is generated from the exact version tag.

- [ ] Start an isolated Fabric 26.3 server with EULA accepted and a fresh temporary world.
- [ ] Verify Polixit loads, `/terrain config` responds, explicit `//pos1`/`//pos2` select a cuboid, `/terrain info` reports data, `/gen` changes blocks, and undo/redo work.
- [ ] Confirm the temporary server shuts down cleanly and the workspace is clean.
- [ ] Document Fabric installation and the procedural fallback difference from Paper's vanilla sampler.
- [ ] Bump the Polixit release version, rebuild the exact tagged Fabric artifact, and rerun module tests.
- [ ] Commit and push release notes/version with required co-author trailers; tag and publish a GitHub release with Fabric jar and matching source archive.

## Execution Notes

- Keep the Bukkit/Paper release artifact and adapter behavior unchanged.
- The first Fabric release uses procedural terrain detail from captured native surface heights; it does not expose Paper's vanilla terrain sampler.
- Commit and push each independently verified feature step.
