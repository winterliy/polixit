import buildlogic.addEngineHubRepository
import buildlogic.internalVersion
import buildlogic.withCuiProtocolDependsOnCommonRule
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RunGameTask
import org.gradle.kotlin.dsl.attributes
import java.net.URI

plugins {
    alias(libs.plugins.fabric.loom.remap)
    id("buildlogic.platform")
    id("buildlogic.validate-minecraft-library-versions-fabric")
}

platform {
    kind = buildlogic.WorldEditKind.Mod
    includeClasspath = true
}

val fabricApiConfiguration: Configuration = configurations.create("fabricApi")

tasks.withType<RunGameTask>().configureEach {
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
}

repositories {
    addEngineHubRepository()
}

// 1.21.11: the remappable common jar is added explicitly below
// withCuiProtocolDependsOnCommonRule(libs.cuiProtocol.fabric.get().module)

dependencies {
    api(project(":worldedit-core"))
    api(project(":worldedit-core-mc"))

    minecraft(libs.fabric.minecraft)
    mappings(loom.officialMojangMappings())
    implementation(libs.fabric.loader)
    modImplementation(libs.cuiProtocol.fabric) { isTransitive = false }
    modImplementation(files(rootProject.file("worldedit-core-mc/build/cui/worldeditcui-protocol-common-mc1.21.11-${libs.versions.cuiProtocol.get()}-remappable.jar")))
    include(libs.cuiProtocol.fabric)

    // [1] Load the API dependencies from the fabric mod json...
    @Suppress("UNCHECKED_CAST")
    val fabricModJson = file("src/main/resources/fabric.mod.json").bufferedReader().use {
        groovy.json.JsonSlurper().parse(it) as Map<String, Map<String, *>>
    }
    val wantedDependencies = (fabricModJson["depends"] ?: error("no depends in fabric.mod.json")).keys
        .filter { it == "fabric-api-base" || it.contains(Regex("v\\d$")) }
        .toSet()
    // [2] Request the matching dependency from fabric-loom
    for (wantedDependency in wantedDependencies) {
        val dep = fabricApi.module(wantedDependency, libs.versions.fabric.api.get())
        include(dep)
        modImplementation(dep)
    }

    // No need for this at runtime
    modCompileOnly(libs.fabric.permissions.api)

    // Silence some warnings, since apparently this isn't on the compile classpath like it should be.
    compileOnly(libs.errorprone.annotations)
}

configure<BasePluginExtension> {
    archivesName.set("polixit-fabric-mc${libs.fabric.minecraft.get().version}")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}

tasks.named<Copy>("processResources") {
    from("../worldedit-bukkit/src/main/resources/MOTOPOLY-LICENSE.txt")
    // Avoid carrying project reference into task execution
    val internalVersion = project.internalVersion
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", internalVersion)
    filesMatching("fabric.mod.json") {
        this.expand(mapOf("version" to internalVersion.get()))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("shaded-dev")
    // Use the JAR output, not classes, as we need jar-in-jar from loom to work properly.
    dependsOn(tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar"))
    val processedFabricModJson = layout.buildDirectory.file("resources/main/fabric.mod.json").get().asFile.absoluteFile
    eachFile {
        // Exclude the fabric.mod.json in the resources folder to allow the one from Fabric's jar task to be added
        if (path == "fabric.mod.json" && file.absoluteFile == processedFabricModJson) {
            exclude()
        }
    }
    from(zipTree(tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").flatMap { it.archiveFile }))
    relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
    relocate("net.royawesome.jlibnoise", "com.sk89q.worldedit.jlibnoise")
    dependencies {
        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("com.sk89q.lib:jlibnoise"))
    }
}

// 1.21.11 is obfuscated: shadowJar also packs the dev-named main classes, so the dist jar takes the
// remapped (intermediary) mod jar first and only adds the remaining shaded libraries from shadowJar.
val distJar = tasks.register<Jar>("distJar") {
    archiveClassifier.set("dist")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val remapJar = tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar")
    val shadowJar = tasks.named<ShadowJar>("shadowJar")
    dependsOn(remapJar, shadowJar)
    from(zipTree(remapJar.flatMap { it.archiveFile }))
    from(zipTree(shadowJar.flatMap { it.archiveFile })) {
        exclude("com/sk89q/worldedit/fabric/**", "fabric.mod.json", "META-INF/jars/**")
    }
}

tasks.named("assemble").configure {
    dependsOn(distJar)
}
