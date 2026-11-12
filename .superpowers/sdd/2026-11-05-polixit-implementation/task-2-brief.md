### Task 2: Brand the replacement Bukkit plugin and set the target runtime

**Files:**
- Modify: `worldedit-bukkit/src/main/resources/plugin.yml`
- Modify: `worldedit-bukkit/build.gradle.kts`
- Modify: root `gradle.properties` only if the existing target release differs from Java 25
- Modify: `worldedit-bukkit/build.gradle.kts` to apply Kotlin only in the Bukkit artifact and name the published artifact Polixit
- Modify: plugin entrypoint metadata and Polixit documentation

- [ ] Add a build assertion for the intended plugin identity, API version 26.3, and Java 25 runtime metadata.
- [ ] Rename plugin display identity to Polixit, set api-version 26.3, add Kotlin runtime loading as needed, and keep the Bukkit main class.
- [ ] Apply the Kotlin JVM plugin only to the Bukkit deliverable; confirm the existing root Java release is 25 and keep unrelated platform modules unchanged.
- [ ] Add a `:worldedit-bukkit:assemble` verification task expectation and inspect generated plugin metadata.

