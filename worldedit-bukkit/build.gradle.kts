plugins {
    id("com.gradleup.shadow")
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-Xjdk-release=1.8")
    }
}

dependencies {
    "implementation"(project(":worldedit-core"))
    "implementation"("com.sk89q:dummypermscompat:1.8")
    "compileOnly"("org.bukkit:bukkit:1.12.2-R0.1-SNAPSHOT")
}

tasks.processResources {
    val v = project.version.toString()
    inputs.property("version", v)
    filesMatching("plugin.yml") { expand("internalVersion" to v) }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("polixit")
    archiveClassifier.set("dist")
    dependencies {
        include(project(":worldedit-core"))
        include(dependency("com.google.code.gson:gson"))
        include(dependency("com.thoughtworks.paranamer:paranamer"))
        include(dependency("com.sk89q:jchronic"))
        include(dependency("com.sk89q.lib:jlibnoise"))
        include(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
    }
    relocate("kotlin", "com.sk89q.worldedit.internal.kotlin")
    dependencies {
    }
    relocate("com.google.gson", "com.sk89q.worldedit.internal.gson")
}
