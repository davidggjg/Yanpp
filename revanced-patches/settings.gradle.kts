rootProject.name = "revanced-patches"

pluginManagement {
    // The settings plugin lives in this repository rather than on a private package
    // registry. Gradle only matches a plugin to an included build when the request
    // carries no version, so the plugins block below asks for the id alone.
    includeBuild("../revanced-patches-gradle-plugin")

    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        // TODO: Remove once https://github.com/google/protobuf-gradle-plugin/pull/797 is merged.
        maven { url = uri("https://jitpack.io") }
    }

    // TODO: Remove once https://github.com/google/protobuf-gradle-plugin/pull/797 is merged.
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.protobuf") {
                useModule("com.github.ReVanced:protobuf-gradle-plugin:${requested.version}")
            }
        }
    }
}

plugins {
    id("app.revanced.patches")
}

settings {
    extensions {
        defaultNamespace = "app.revanced.extension"

        // Must resolve to an absolute path (not relative),
        // otherwise the extensions in subfolders will fail to find the proguard config.
        proguardFiles(rootProject.projectDir.resolve("extensions/proguard-rules.pro").toString())
    }
}

include(":patches:stub")

// Resolve the patcher from this repository instead of a package registry.
file("../revanced-patcher").let { libDir ->
    if (libDir.exists()) {
        includeBuild(libDir) {
            dependencySubstitution {
                substitute(module("app.revanced:revanced-patcher")).using(project(":patcher"))
            }
        }
    }
}