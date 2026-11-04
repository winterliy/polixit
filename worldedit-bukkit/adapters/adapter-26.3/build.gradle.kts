import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension

plugins {
    id("buildlogic.adapter-mojmap")
    kotlin("jvm") version "2.4.20"
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
    }
}

dependencies {
    // https://artifactory.papermc.io/ui/native/universe/io/papermc/paper/dev-bundle/
    the<PaperweightUserDependenciesExtension>().paperDevBundle("26.3.build.190-stable")
}
