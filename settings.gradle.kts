import java.util.Locale

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.canvasmc.io/public")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "baguette"
for (name in listOf("baguette-api", "baguette-server")) {
    val projName = name.lowercase(Locale.ENGLISH)
    include(projName)
    findProject(":$projName")!!.projectDir = file(name)
}

gradle.lifecycle.beforeProject {
    val mcVersion = providers.gradleProperty("mcVersion").get().trim()
    val baguetteChannel = providers.gradleProperty("channel").get().trim()
    val baguetteBuildNumber = providers.environmentVariable("BUILD_NUMBER").orNull?.trim()?.toInt()
    val versionString = if (baguetteBuildNumber == null) {
        "$mcVersion.local-SNAPSHOT"
    } else {
        "$mcVersion.build.$baguetteBuildNumber-${baguetteChannel.lowercase()}"
    }
    version = versionString
}
