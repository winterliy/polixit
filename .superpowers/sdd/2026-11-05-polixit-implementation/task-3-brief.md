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

