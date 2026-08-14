package app.morphe.patches.gradle

import org.gradle.api.Plugin
import org.gradle.api.UnknownProjectException
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import java.net.URI
import javax.inject.Inject

@Suppress("unused")
abstract class SettingsPlugin @Inject constructor(
    private val objectFactory: ObjectFactory,
) : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension = settings.extensions.create("settings", SettingsExtension::class.java)

        settings.gradle.settingsEvaluated {
            settings.gradle.sharedServices.registerIfAbsent(
                "settingsExtensionProvider",
                SettingsExtensionProvider::class.java,
            ) {
                it.parameters.apply {
                    defaultNamespace = extension.extensions.defaultNamespace
                    proguardFiles = extension.extensions.proguardFiles
                }
            }
        }

        settings.configureDependencies()
        settings.configureProjects(extension)
    }

    /**
     * Add required repositories.
     */
    private fun Settings.configureDependencies() {
        @Suppress("UnstableApiUsage")
        dependencyResolutionManagement.repositories.apply {
            mavenLocal()
            mavenCentral()
            google()
            maven { repository -> repository.url = URI("https://jitpack.io") }

            // The private package registry is only usable when credentials are actually
            // present. Adding it unconditionally used to blow up with a bare
            // IllegalArgumentException, because Provider.orElse rejects a null fallback
            // and GITHUB_TOKEN is unset outside of a workflow that passes it in.
            // Everything this build needs resolves locally, so the registry is optional.
            val gprUser = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            val gprKey = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")

            if (!gprUser.isNullOrBlank() && !gprKey.isNullOrBlank()) {
                maven { repository ->
                    // A repository must be specified. "registry" is a dummy.
                    repository.url = URI("https://maven.pkg.github.com/MorpheApp/registry")
                    repository.credentials {
                        it.username = gprUser
                        it.password = gprKey
                    }
                }
            }
        }
    }

    /**
     * Adds the required plugins to the patches and extension projects.
     */
    private fun Settings.configureProjects(extension: SettingsExtension) {
        // region Include the projects

        val extensionsProjectPath = extension.extensions.projectsPath

        if (extensionsProjectPath != null) {
            objectFactory.fileTree().from(rootDir.resolve(extensionsProjectPath)).matching {
                it.include("**/build.gradle.kts")
            }.forEach {
                include(it.parentFile.relativeTo(rootDir).toPath().joinToString(":"))
            }
        }

        include(extension.patchesProjectPath)

        // endregion

        // region Apply the plugins

        gradle.rootProject { rootProject ->
            if (extensionsProjectPath != null) {
                val extensionsProject = try {
                    rootProject.project(extensionsProjectPath)
                } catch (e: UnknownProjectException) {
                    null
                }

                extensionsProject?.subprojects { extensionProject ->
                    if (
                        extensionProject.buildFile.exists() &&
                        !extensionProject.parent!!.plugins.hasPlugin(ExtensionPlugin::class.java)
                    ) {
                        extensionProject.pluginManager.apply(ExtensionPlugin::class.java)
                    }
                }
            }

            // Needs to be applied after the extension plugin
            // so that their extensionConfiguration is available for consumption.
            rootProject.project(extension.patchesProjectPath).pluginManager.apply(PatchesPlugin::class.java)
        }

        // endregion
    }
}
