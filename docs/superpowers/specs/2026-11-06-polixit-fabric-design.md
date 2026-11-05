# Polixit Fabric 26.3 Design

## Goal

Ship a Fabric 26.3 Polixit mod with the same terrain commands as the Bukkit/Paper build while retaining WorldEdit selection and history behavior.

## Current State

- The repository is a WorldEdit fork targeting Minecraft 26.3.
- `worldedit-fabric` already builds and registers shared WorldEdit commands through Fabric's command API.
- Terrain command handlers live in `worldedit-core`; Fabric currently returns no `TerrainService`.
- Motopoly-derived terrain planning algorithms are pure Java but currently live in the Bukkit module. Surface scanning and block application are Bukkit-specific.
- The Paper build identifies itself as Polixit. Fabric metadata and entrypoint still identify as WorldEdit.

## Scope

- Fabric mod artifact for Minecraft 26.3, named Polixit.
- Reuse shared pure Java terrain algorithms from `worldedit-core`.
- Add a Fabric terrain service for `/gen [y]`, `/terrain info`, and per-player `/terrain config`.
- Capture world state on the server thread, compute terrain plans asynchronously, and apply bounded block batches on server ticks through `EditSession` and `LocalSession`.
- Preserve per-player undo/redo for generated terrain.
- Keep the existing Bukkit/Paper implementation behavior unchanged.

## Design Decisions

- Move only platform-neutral algorithm classes (`GenerateParams`, `GeneratePlan`, noise, ore table, `TerrainGenerator`, and `TerrainStats`) to `worldedit-core` under `com.sk89q.worldedit.terrain`.
- Keep Bukkit-specific snapshots, vanilla terrain sampling, materials, and persistence in the Bukkit module.
- Implement Fabric scanning against native `ServerLevel`/chunk data, with a bounded selection size and no off-thread live-world access.
- Use Fabric's server tick event to drain queued edit batches. Use the standard WorldEdit history implementation for Fabric so `/undo` and `/redo` operate through the shared `LocalSession`.
- Store Fabric terrain options in Fabric's config directory by player UUID; do not modify the Bukkit configuration format.
- Rename Fabric metadata/entrypoint lookup to Polixit while retaining WorldEdit's internal Java package names and CUI protocol dependency.

## Acceptance Criteria

- Core and Bukkit algorithm tests continue to pass after the move.
- Fabric module compiles and produces a standalone Fabric mod jar containing the terrain service and Polixit metadata.
- A Fabric 26.3 server starts with the mod and no Bukkit/Paper runtime dependency.
- `//pos1 x y z`, `//pos2 x y z`, `/terrain info`, `/terrain config`, and `/gen [y]` are registered and callable.
- A new Fabric world can generate terrain from an explicit selection; generated changes are visible in the world and can be undone and redone.
- The tested Fabric artifact and matching source are published as a Polixit release.

## Risks and Limits

- Fabric does not expose Bukkit chunk snapshots or Paper's vanilla terrain sampler. The first Fabric implementation will use captured native height and block data with the shared procedural fallback, rather than Paper's adapter-provided vanilla detail.
- Large selections must be capped and block writes spread across ticks to avoid server stalls.
- Existing Fabric installations use the upstream `worldedit` mod id; changing the mod id to `polixit` means users must remove the upstream jar and may need to move its config manually.
