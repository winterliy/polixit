import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import buildlogic.CuiProtocolCommonIsNotFabricSpecificRule
import buildlogic.addEngineHubRepository

plugins {
    alias(libs.plugins.fabric.loom.remap)
    id("buildlogic.core-and-platform")
    id("buildlogic.expose-resources")
}

description = "The Minecraft-specific implementation of WorldEdit's core module." +
        " This code is shared by all platforms that rely on access without an API."

repositories {
    addEngineHubRepository()
}

dependencies {
    api(project(":worldedit-core"))
    api(project(":worldedit-libs:core-mc"))

    minecraft(libs.fabric.minecraft)
    mappings(loom.officialMojangMappings())

    // Provided by platforms.
    compileOnly(libs.fabric.mixin)

    // The 1.21.11 CUI common jar is intermediary-named but has no fabric.mod.json, so Loom would not
    // remap it. Copy it with the Fabric-Loom-Remap manifest flag so it is remapped like a mod.
    modImplementation(files(remappableCuiCommon()))
    components {
        withModule<CuiProtocolCommonIsNotFabricSpecificRule>(libs.cuiProtocol.common.get().module)
    }

    // Silence some warnings, since apparently this isn't on the compile classpath like it should be.
    compileOnly(libs.errorprone.annotations)
}

base {
    archivesName.set("${project.name}-mc${libs.fabric.minecraft.get().version}")
}

publishing {
    publications.named<MavenPublication>("maven") {
        artifactId = base.archivesName.get()
        from(components["java"])
    }
}

fun remappableCuiCommon(): File {
    val source = configurations.detachedConfiguration(
        dependencies.create(libs.cuiProtocol.common.get().toString()).also { (it as ExternalModuleDependency).isTransitive = false }
    ).singleFile
    val target = layout.buildDirectory.file("cui/${source.nameWithoutExtension}-remappable.jar").get().asFile
    if (!target.exists()) {
        target.parentFile.mkdirs()
        ZipInputStream(source.inputStream()).use { input ->
            ZipOutputStream(target.outputStream()).use { output ->
                var entry = input.nextEntry
                while (entry != null) {
                    output.putNextEntry(ZipEntry(entry.name))
                    if (entry.name == "META-INF/MANIFEST.MF") {
                        val text = input.readBytes().toString(Charsets.UTF_8).trimEnd()
                        output.write((text + "\r\nFabric-Loom-Remap: true\r\n\r\n").toByteArray())
                    } else {
                        input.copyTo(output)
                    }
                    output.closeEntry()
                    entry = input.nextEntry
                }
            }
        }
    }
    return target
}
