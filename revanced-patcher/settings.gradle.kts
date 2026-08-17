rootProject.name = "revanced-patcher"

// TYPESAFE_PROJECT_ACCESSORS was enabled here but nothing in this build uses a
// projects.* accessor. It is not harmless: this build is included by
// revanced-patches, so the preview applied there too, and generating the
// accessors fails on Gradle 9.6.1 with
//   AbstractMethodError: RootProjectAccessor_Decorated does not define
//   ExtensionAware.getExtensions()

pluginManagement {
    repositories {
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()

        // app.revanced:apktool-lib is not on Maven Central, Google's Maven or JitPack -
        // it only exists on GitHub Packages. GitHub Packages Maven registries require a
        // token even for public packages, so these repositories are only registered when
        // one is available; without it they would turn a missing artifact into a
        // confusing authentication failure instead.
        val githubUser = providers.gradleProperty("gpr.user").orNull
            ?: System.getenv("GITHUB_ACTOR")
        val githubToken = providers.gradleProperty("gpr.key").orNull
            ?: System.getenv("GITHUB_TOKEN")

        if (!githubUser.isNullOrBlank() && !githubToken.isNullOrBlank()) {
            listOf("revanced-patcher", "apktool").forEach { repository ->
                maven {
                    name = "githubPackages-$repository"
                    url = uri("https://maven.pkg.github.com/revanced/$repository")
                    credentials {
                        username = githubUser
                        password = githubToken
                    }
                }
            }
        }
    }
}

include(":patcher")
