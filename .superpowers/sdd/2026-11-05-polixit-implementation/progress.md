# SDD ledger — plan: docs/superpowers/plans/2026-11-05-polixit-implementation.md

Base: EngineHub/WorldEdit version/7.4.x commit as cloned in winterliy/polixit.

Pre-flight: Task 3 algorithms are consumed by Task 6 job coordinator; use a stable GeneratePlan + ChangeSet computation boundary. Task 4 EditSessionBuilder.changeSet(ChangeSet) is consumed by Task 6; task coordinator builds a dedicated EditSession and records it in LocalSession only after success. Task 5 core TerrainService capability is implemented by the Bukkit service from Task 6; keep Bukkit types out of the core interface. No other shared interfaces.

Ruling: Keep WorldEditPlugin as JavaPlugin main class while changing plugin.yml display identity to Polixit — WorldEdit class lookups and plugin binary entry remain compatible where they use class APIs; plugin-name lookups and data folder intentionally become Polixit as the requested replacement plugin — cost if wrong: plugins requiring the plugin name WorldEdit or WorldEdit's previous config path will need migration.

Task 1: complete (commit docs/spec and plan; fork and branch created; no tests run)
Task 1: baseline verification — ./gradlew :worldedit-bukkit:test --no-daemon --console=plain → BUILD SUCCESSFUL, 51 actionable tasks (23 executed, 28 up-to-date).
Task 2: Ruling: gradle/libs.versions.toml already sets primaryJavaRelease=25 — preserve existing 25 target and avoid a broad Java-target change — cost if wrong: an adapter using a stricter target may need a scoped exception.
Task 2: Ruling: package only adapter-26.3 — design limits Polixit to GLAP1/Paper 26.3, so bundling and remapping 1.21.x adapters is unnecessary — cost if wrong: users on older Paper versions cannot use this jar.
Task 2: complete (test command: ./gradlew :worldedit-bukkit:test :worldedit-bukkit:assemble --no-daemon --console=plain → BUILD SUCCESSFUL; 61 actionable tasks; jar contains plugin.yml and v26_3 adapter).
