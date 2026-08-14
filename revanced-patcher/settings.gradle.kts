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

        // Only usable with credentials, and everything this build needs comes from
        // the repositories above - registering it unconditionally just turns a
        // missing artifact into a confusing authentication failure.
        val githubUser = providers.gradleProperty("gpr.user").orNull
            ?: System.getenv("GITHUB_ACTOR")
        val githubToken = providers.gradleProperty("gpr.key").orNull
            ?: System.getenv("GITHUB_TOKEN")

        if (!githubUser.isNullOrBlank() && !githubToken.isNullOrBlank()) {
            maven {
                name = "githubPackages"
                url = uri("https://maven.pkg.github.com/revanced/revanced-patcher")
                credentials {
                    username = githubUser
                    password = githubToken
                }
            }
        }
    }
}

include(":patcher")
