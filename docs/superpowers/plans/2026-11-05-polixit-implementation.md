# Polixit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Create a public Polixit fork of WorldEdit with Motopoly terrain generation integrated into the Bukkit/Paper plugin for GLAP1 26.3.

**Architecture:** Fork EngineHub/WorldEdit version/7.4.x and preserve WorldEdit's full existing implementation. Integrate Motopoly generation in the Bukkit module, route selection through WorldEdit, apply edits through EditSession and LocalSession history, and use a compact palette-based ChangeSet for large terrain operations. Expose Bukkit generation through a core platform capability so shared command parsing can invoke it without a core-to-Bukkit dependency. Keep Paper/NMS entry points in Java and use Kotlin for internal terrain orchestration.

**Tech Stack:** Java 25, Gradle Kotlin DSL, Kotlin 2.4.20, Paper 26.3, WorldEdit 7.4.x, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-11-05-polixit-design.md`

## Global Constraints

- Runtime target is Paper 26.3 / GLAP1 with Java 25 or newer.
- Polixit is a single replacement plugin; the original WorldEdit jar must not be installed alongside it.
- WorldEdit's GPLv3 notices and Motopoly's MIT notice must remain present.
- //gen [y] is Motopoly generation; formula generation remains //generate and //g.
- WorldEdit's native //pos1, //pos2, wand, //undo, and //redo remain the selection/history interface.
- Keep common WorldEdit platform modules at their existing Java release unless a direct dependency requires a change.
- Release from winterliy/polixit with source corresponding to the distributed jar.

## Review Focus

- Command conflict: tests prove //gen resolves to terrain generation while //generate and //g retain formula generation.
- Invalid/missing selection and cross-world selection: command reports a useful error without starting work.
- Cancelled/failed asynchronous job: partially applied blocks are restored and no incomplete EditSession is remembered.
- Large change sets: compressed history undo/redo restores exact block data without exceeding configured history bounds.
- World height, water, and chunk edge boundaries: terrain sampling and application stay within Paper 26.3 limits and loaded chunks.

---

### Task 1: Create the Polixit source repository

**Files:**
- Create: `~/Desktop/polixit` as a fork clone of EngineHub/WorldEdit `version/7.4.x`
- Preserve: `LICENSE.txt`, source headers, upstream remote
- Add: `docs/superpowers/specs/2026-11-05-polixit-design.md`

- [ ] Fork EngineHub/WorldEdit as winterliy/polixit, retain EngineHub as upstream, and check out the 7.4.x branch in the requested Desktop folder.
- [ ] Copy the approved design document into the repository.
- [ ] Confirm the working tree and remotes identify polixit as origin and EngineHub/WorldEdit as upstream.

### Task 2: Brand the replacement Bukkit plugin and set the target runtime

**Files:**
- Modify: `worldedit-bukkit/src/main/resources/plugin.yml`
- Modify: `worldedit-bukkit/build.gradle.kts`
- Modify: root `gradle.properties` only if the existing target release differs from Java 25
- Modify: `worldedit-bukkit/build.gradle.kts` to apply Kotlin only in the Bukkit artifact and name the published artifact Polixit
- Modify: plugin entrypoint metadata and Polixit documentation
- Test: `worldedit-bukkit/src/test/java/com/sk89q/worldedit/bukkit/PolixitPluginMetadataTest.java`

- [ ] Add a JUnit assertion for the intended plugin identity, API version 26.3, Kotlin runtime, and Java 25 class-file target.
- [ ] Rename plugin display identity to Polixit, set api-version 26.3, add Kotlin runtime loading as needed, and keep the Bukkit main class.
- [ ] Apply the Kotlin JVM plugin only to the Bukkit deliverable; confirm the existing root Java release is 25 and keep unrelated platform modules unchanged.
- [ ] Run `:worldedit-bukkit:test :worldedit-bukkit:assemble` and inspect the generated plugin metadata and adapter contents.

### Task 3: Port Motopoly terrain model and algorithms

**Files:**
- Create: Bukkit-internal Java terrain algorithm sources corresponding to Motopoly `src/main/java/dev/motopoly/terrain/*`
- Create: Bukkit-internal Kotlin terrain pipeline sources corresponding to Motopoly `src/main/kotlin/dev/motopoly/world/*` and Settings
- Create: migrated terrain unit tests in the Bukkit module
- Add: Motopoly MIT attribution/license notice

- [ ] Port `GenerateParams`, `GeneratePlan`, noise, ore table, terrain generation, surface scanner, vanilla terrain sampling, vegetation, water, and change planning into Polixit packages.
- [ ] Replace direct Motopoly plugin/world dependencies with narrow Bukkit/WorldEdit adapters; retain deterministic generation and configured fill limits.
- [ ] Port existing TerrainGenerator and OreTable tests and adapt them to the new package/API.
- [ ] Run the migrated unit tests and module compilation.

### Task 4: Add compressed WorldEdit history for terrain edits

**Files:**
- Modify: `worldedit-core/src/main/java/com/sk89q/worldedit/EditSession.java`
- Modify: `worldedit-core/src/main/java/com/sk89q/worldedit/EditSessionBuilder.java`
- Create: Bukkit-local packed palette `ChangeSet` implementation and tests

**Interfaces:**
- `EditSessionBuilder.changeSet(ChangeSet)` injects a history implementation into the built session.
- Bukkit terrain generation uses a packed palette ChangeSet that implements WorldEdit `ChangeSet`; its iterators return normal `BlockChange` values for LocalSession undo/redo.

- [ ] Add tests proving packed changes preserve previous/current block data and reverse/forward iteration order.
- [ ] Verify tests fail before implementing the packed ChangeSet.
- [ ] Implement builder injection with the existing `BlockOptimizedHistory` as the default.
- [ ] Implement packed position and block-state palette storage, delegating non-block `Change` types safely.
- [ ] Verify tests pass and existing core compilation remains intact.

### Task 5: Integrate native WorldEdit selection and terrain commands

**Files:**
- Modify: `worldedit-core/src/main/java/com/sk89q/worldedit/command/GenerationCommands.java`
- Modify: generated command registrations/source annotations for GenerationCommands
- Modify: `worldedit-core/src/main/java/com/sk89q/worldedit/extension/platform/Platform.java`
- Modify: `worldedit-core/src/main/java/com/sk89q/worldedit/extension/platform/PlatformCommandManager.java`
- Create: core `TerrainService` capability and `TerrainCommands` command registration
- Create: Bukkit terrain-service implementation and platform wiring in `BukkitServerInterface`

**Interfaces:**
- `//gen [y]` accepts the WorldEdit `Region` selection and starts a terrain job through the optional platform TerrainService capability.
- `//generate` and `//g` continue to accept the existing pattern/expression arguments.
- `//terrain config|info` exposes Motopoly settings and terrain analysis.

- [ ] Add command registration tests for the new //gen mapping and preserved formula command aliases.
- [ ] Verify the existing alias behavior is captured before changing it.
- [ ] Update generation registration so //gen is removed from formula aliases and registered to terrain generation while //generate and //g remain.
- [ ] Read selected WorldEdit region/world and selection corner Y values; reject absent, unsupported, or invalid regions before job creation.
- [ ] Register a shared core command backed by an optional Platform TerrainService; implement the service in Bukkit without adding Bukkit dependencies to WorldEdit core.
- [ ] Verify command tests and module compilation.

### Task 6: Run asynchronous terrain work through EditSession and LocalSession

**Files:**
- Create: Bukkit terrain job/coordinator and cancellation state
- Modify: Bukkit command/service classes, Polixit configuration, and command messages
- Modify: WorldEdit history wiring only if required by the job lifecycle

- [ ] Add tests for success, cancellation, sampling failure, application failure, and history registration lifecycle.
- [ ] Compute snapshots/plans asynchronously without accessing mutable Bukkit world state off-thread.
- [ ] Apply block edits on the server thread in configured per-tick batches through an EditSession using packed ChangeSet.
- [ ] On complete, close and remember the EditSession once; on failure/cancel, restore applied edits and do not add incomplete history.
- [ ] Add `//terrain config` persistence for distance, round, rough, step, plus `//terrain info` scan summaries.
- [ ] Verify job lifecycle tests and inspect that //undo and //redo use the same LocalSession.

### Task 7: Finish docs, build, release, and publish

**Files:**
- Modify: `README.md`, changelog/version files, workflow/release metadata
- Add: release notes and GPLv3/MIT attribution notes as needed

- [ ] Document install/upgrade, original WorldEdit removal, command alias change, Java/Paper requirements, settings, undo behavior, and source/license terms.
- [ ] Run the complete Gradle build and verify the bundled plugin jar loads its Kotlin runtime and contains the 26.3 adapter.
- [ ] Run a Paper 26.3/GLAP1 smoke check for plugin load, selection, both generation commands, terrain settings/info, undo/redo, and cancellation.
- [ ] Update version and changelog, commit each coherent change with required co-author trailers, push the Polixit branch, create a version tag, and publish the GitHub release with jar and corresponding source.
- [ ] Confirm release URL, attached artifact, and GitHub source visibility.
