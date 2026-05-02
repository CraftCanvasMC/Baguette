import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("io.canvasmc.weaver.patcher") version "2.4.3" // always keep in check with canvas's actual used release
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

paperweight {
    // This controls the patch filtering setting
    // It controls whether empty patches should be deleted automatically or kept
    // the default value is true but it can sometimes break git's 3way apply in rare cases, so it's left configurable
    // NOTE: this option is duplicated in the server build file, so make sure to set it to what you like too. What you set here doesn't get respected there automatically.
    filterPatches = true
    upstreams.canvas {
        ref = providers.gradleProperty("canvasCommit")

        patchFile {
            path = "canvas-server/build.gradle.kts"
            outputFile = file("baguette-server/build.gradle.kts")
            patchFile = file("baguette-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "canvas-api/build.gradle.kts"
            outputFile = file("baguette-api/build.gradle.kts")
            patchFile = file("baguette-api/build.gradle.kts.patch")
        }
        patchRepo("paperApi") {
            upstreamPath = "paper-api"
            patchesDir = file("baguette-api/paper-patches")
            // Thanks to weaver, you can also use ATs for all sources, not just the minecraft one.
            // By default, when weaver is looking for an AT file for a patch source set, it looks under the `build-data` dir for an AT file under the name of the patch set.
            // For this patchRepo, it would look for paperApi.at because 'paperApi` is the name of the source set, as declared in the `patchRepo("paperApi")` field.
            // If you want to override either the location of build-data dir or the at file itself you can do so by modifying the `buildDataDir` and/or `additionalAts` fields.
            // An important behavior change compared to paperweight in regards to the minecraft AT file is the added possibility to specify ats for libraries instead of having to patch them manually.
            outputDir = file("paper-api")
        }
        patchRepo("foliaApi") {
            upstreamPath = "folia-api"
            patchesDir = file("baguette-api/folia-patches")
            outputDir = file("folia-api")
        }
        patchDir("canvasApi") {
            upstreamPath = "canvas-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch", "paper-patches", "folia-patches")
            patchesDir = file("baguette-api/canvas-patches")
            outputDir = file("canvas-api")
	    }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
        options.isFork = true
        options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
    }
    tasks.withType<Javadoc>().configureEach {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test>().configureEach {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }
}
