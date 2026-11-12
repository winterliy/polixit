pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "worldedit"

include("worldedit-libs")

// Polixit for Minecraft 1.13.2 - 1.16.5 ships upstream's prebuilt legacy adapters.
listOf("legacy").forEach {
    include("worldedit-bukkit:adapters:adapter-$it")
}

listOf("bukkit", "core", "cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")
