// Polixit for Minecraft 1.12.2 / 1.8.8: WorldEdit 6.1.9 rebuilt with a current Gradle (the original build
// relied on Gradle 4 and JCenter). Everything compiles to Java 8 bytecode; Jabel allows the newer syntax
// used by Polixit's terrain code.
plugins {
    java
    id("com.gradleup.shadow") version "8.3.5" apply false
    kotlin("jvm") version "2.0.21" apply false
}

allprojects {
    group = "com.sk89q.worldedit"
    version = "0.2.0"
    repositories {
        mavenCentral()
        maven("https://maven.enginehub.org/repo/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

subprojects {
    apply(plugin = "java-library")
    extensions.configure<JavaPluginExtension> { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }
    dependencies {
        "annotationProcessor"("com.pkware.jabel:jabel-javac-plugin:1.0.1-2")
        "compileOnly"("com.pkware.jabel:jabel-javac-plugin:1.0.1-2")
    }
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "17"
        options.release.set(8)
        options.encoding = "UTF-8"
    }
}
