import java.util.Locale

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.canvasmc.io/releases")
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
