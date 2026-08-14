rootProject.name = "morphe-patches"

pluginManagement {
    // The settings plugin lives in this repository rather than on a package registry.
    // Gradle only matches a plugin to an included build when the request carries no
    // version, so the plugins block below asks for the id alone.
    includeBuild("../morphe-patches-gradle-plugin")

    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        // Obtain baksmali/smali from source builds - https://github.com/iBotPeaches/smali
        // Remove when official smali releases come out again.
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("app.morphe.patches")
}

settings {
    extensions {
        defaultNamespace = "app.morphe.extension"

        // Must resolve to an absolute path (not relative),
        // otherwise the extensions in subfolders will fail to find the proguard config.
        proguardFiles(rootProject.projectDir.resolve("extensions/proguard-rules.pro").toString())
    }
}

include(":patches:stub")

// Include morphe-patcher as composite builds if they exist locally
mapOf(
    "morphe-patcher" to "app.morphe:morphe-patcher",
).forEach { (libraryPath, libraryName) ->
    val libDir = file("../$libraryPath")
    if (libDir.exists()) {
        includeBuild(libDir) {
            dependencySubstitution {
                substitute(module(libraryName)).using(project(":"))
            }
        }
    }
}

// Include morphe-patches-library as composite build if it exists locally.
// It is a multi-module project, so each artifact maps to a specific subproject.
file("../morphe-patches-library").let { libDir ->
    if (libDir.exists()) {
        includeBuild(libDir) {
            dependencySubstitution {
                substitute(module("app.morphe:morphe-patches-library")).using(project(":patch-library"))
                substitute(module("app.morphe:morphe-extensions-library")).using(project(":extension-library"))
            }
        }
    }
}
