import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import io.papermc.paperweight.core.tasks.patching.ApplyBasePatches
import io.papermc.paperweight.core.tasks.patching.ApplyFeaturePatches
import io.papermc.paperweight.tasks.RebuildBaseGitPatches
import io.papermc.paperweight.tasks.RebuildGitPatches
import io.papermc.paperweight.tasks.CreatePublisherJar

plugins {
    java
    id("io.canvasmc.weaver.patcher") version "2.3.10" // always keep in check with canvas's actual used release
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

paperweight {
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
            additionalAts = file("build-data/baguette-paperapi.at") // custom at for paper-api sources
            // This line above and the relevant lines below allow us to specify ATs for non-minecraft sources thanks to weaver.
            // You can set all relevant lines here and in the server build file to the same at file eg. `baguette.at`, however doing that is *discouraged*
            // due to us possibly enabling AT validation in the future, which would result in errors when an AT cannot apply.
            // Setting each patch set to have its own AT file is good practice and should futureproof you from any inconveniences further down the line, should we change anything.
            // If additionalAts is not specified, weaver *won't* fallback to the general AT file due to the aforementioned reasons
            // An important behavior change compared to paperweight in regards to the minecraft AT file is the added possibility to specify ats for libraries instead of having to patch them manually.
            outputDir = file("paper-api")
        }
        patchRepo("foliaApi") {
            upstreamPath = "folia-api"
            patchesDir = file("baguette-api/folia-patches")
            additionalAts = file("build-data/baguette-foliaapi.at") // custom at for folia-api sources
            outputDir = file("folia-api")
        }
        patchDir("canvasApi") {
            upstreamPath = "canvas-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch", "paper-patches", "folia-patches")
            patchesDir = file("baguette-api/canvas-patches")
            additionalAts = file("build-data/baguette-canvasapi.at") // custom at for canvas-api sources
            outputDir = file("canvas-api")
	}
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
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
allprojects {
    // This block controls the patch filtering setting
    // It controls whether empty patches should be deleted automatically or kept
    // the default value is true but it can sometimes break git's 3way apply in rare cases, so it's left configurable
    tasks.withType<RebuildBaseGitPatches>().configureEach {
        filterPatches = true
    }
    tasks.withType<RebuildGitPatches>().configureEach {
        filterPatches = true
    }

    // This block on the other hand showcases how to enable an opt-in property which changes the way base and feature patches apply.
    // By default when there are any apply conflicts, the patch fails to apply *completely* and doesn't continue the apply.
    // The `emitRejects` property allows to change this behaviour to make it instead *always* continue the apply, even when most hunks didn't apply
    // and leaves the repository in a partially applied state, while emitting `.rej` files which contain failed hunks, each named by the file the failed hunk was modifying
    // This behaviour can be useful in case you have a lot of involving patches that break on upstream updates frequently, so this way everything that can apply, gets applied and the unapplied parts
    // are emitted as .rej files, you can apply manually and then continue the `git am` session after you've done the manual apply
    // There are also more verbose details provided in the log file, such as the exact code snippets; see the console output on where to find it
    // note: it is important you *don't* forget to remove the leftover `.rej` files as they WILL be added to your patch when you use `git add .` if you don't remove them
    tasks.withType<ApplyBasePatches>().configureEach {
        emitRejects = false
    }
    tasks.withType<ApplyFeaturePatches>().configureEach {
        emitRejects = false
    }
}

// Weaver also provides an useful `create(Mojmap/Reobf)PublisherJar` task which generates a paperclip jar with the build number or whatever input you give it
// The default output of the task is determined as follows: `[project name lowercase]-build.[the build number or local when there's none].jar`
// Following that, we can deduct that the name for our Baguette fork would be either `baguette-build.1.jar` or `baguette-build.local.jar` when there's no `BUILD_NUMBER` environment variable set
// An example *custom* configuration is shown here
/*
// custom input for publisherJar
val buildNumber = providers.environmentVariable("BUILD_NUM").orElse("no-build")
val jarName = buildNumber.map { build -> "libs/output-$build-test.jar" }

subprojects {
    tasks.withType<CreatePublisherJar>().configureEach {
        outputZip.set(layout.buildDirectory.file(jarName))
    }
}
*/

