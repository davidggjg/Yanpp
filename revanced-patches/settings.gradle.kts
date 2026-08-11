rootProject.name = "revanced-patches"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        // TODO: Remove once https://github.com/google/protobuf-gradle-plugin/pull/797 is merged.
        maven { url = uri("https://jitpack.io") }
    }

    // The settings plugin lives in this repository rather than on a private package
    // registry, so include it as a composite build when it is present locally.
    file("../revanced-patches-gradle-plugin").let { pluginDir ->
        if (pluginDir.exists()) {
            includeBuild(pluginDir)
        }
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
    id("app.revanced.patches") version "1.3.3"
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