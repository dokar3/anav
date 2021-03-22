package com.dokar.anav.plugin

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidBasePlugin
import com.android.ide.common.symbols.getPackageNameFromManifest
import org.gradle.api.*
import org.gradle.api.tasks.TaskContainer
import org.gradle.process.CommandLineArgumentProvider
import java.io.File

class AnavPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // dsl configuration
        val navExtension = target.extensions.create(
            "anavConfig",
            AnavPluginExtension::class.java
        )

        // AnnotationProcessor argumentProvider
        val argProvider = ArgumentProvider(emptyMap())

        if (target.isRootProject()) {
            // Configure all Android sub projects
            target.allprojects { project ->
                project.afterEvaluate {
                    if (!project.isAndroidProject()) {
                        // Skip non-Android projects
                        return@afterEvaluate
                    }
                    project.setCompilerArgumentProvider(argProvider)
                    project.setupAnavOptions(navExtension, argProvider)
                }
            }
        } else {
            check(target.isAndroidProject()) {
                "Anav have not configured, only Android projects/modules" +
                        " are supported."
            }
            target.setCompilerArgumentProvider(argProvider)
            target.gradle.afterProject {
                it.setupAnavOptions(navExtension, argProvider)
            }
        }
    }

    private fun Project.setCompilerArgumentProvider(
        argProvider: ArgumentProvider
    ) {
        log("Anav: Run project: ${project.name}, path: ${project.projectDir}")

        val androidExtension = project.extensions.findByType(
            BaseExtension::class.java
        ) ?: error("Anav: No android extension found")

        // set argumentProvider
        androidExtension.defaultConfig
            .javaCompileOptions
            .annotationProcessorOptions
            .compilerArgumentProvider(argProvider)
    }

    /**
     * Get arguments from extension
     * */
    private fun Project.setupAnavOptions(
        anavExtension: AnavPluginExtension,
        argProvider: ArgumentProvider
    ) {
        DEBUG = anavExtension.debug

        val baseModule = anavExtension.baseModule

        if (!baseModule.isNullOrEmpty()) {
            log("Anav: baseModule: $baseModule")

            val baseModuleProject = try {
                rootProject.project(baseModule)
            } catch (e: UnknownProjectException) {
                error(
                    "Anav: Cannot find the base module: $baseModule\n" +
                            MSG_BASE_MODULE_NOT_FOUND
                )
            }

            if (baseModuleProject.isAndroidExtAvailable()) {
                setupAnavOptionsWithBaseModule(
                    baseModuleProject,
                    anavExtension,
                    argProvider
                )
            } else {
                baseModuleProject.afterEvaluate { baseProject ->
                    setupAnavOptionsWithBaseModule(
                        baseProject,
                        anavExtension,
                        argProvider
                    )
                }
            }
        } else {
            var packageName = anavExtension.packageName
            if (packageName == null) {
                packageName = findAppIdFromManifest(this)?.let {
                    it + DEFAULT_PKG_SUFFIX
                }
            }
            updateCompilerOptions(
                argProvider = argProvider,
                buildDir = null,
                sourceDir = null,
                packageName = packageName,
                navMapClassName = anavExtension.navMapClassName,
                navArgsClassName = anavExtension.navArgsClassName,
                removeActivitySuffix = anavExtension.removeActivitySuffix
            )
        }
    }

    private fun setupAnavOptionsWithBaseModule(
        baseProject: Project,
        anavExtension: AnavPluginExtension,
        argProvider: ArgumentProvider
    ) {
        val baseModule = anavExtension.baseModule

        var packageName = anavExtension.packageName
        if (packageName.isNullOrEmpty()) {
            packageName = findAppIdFromManifest(baseProject)
            if (packageName.isNullOrEmpty()) {
                error(
                    "Anav: Cannot find applicationId from baseModule: $baseModule" +
                            ", your AndroidManifest.xml file in base module" +
                            " must contain 'package' attribute."
                )
            }
            packageName += DEFAULT_PKG_SUFFIX
        }

        val baseModuleBuildDir = baseProject.buildDir.absolutePath

        val javaSourceSet = baseProject.extensions
            .getByType(BaseExtension::class.java)
            .sourceSets
            .find {
                it.name == "main" && it.java.srcDirs.isNotEmpty()
            } ?: error("Anav: Cannot find source set for module: $baseModule")

        val baseModuleSourceDir = javaSourceSet.java.srcDirs.first().absolutePath

        updateCompilerOptions(
            argProvider = argProvider,
            buildDir = baseModuleBuildDir,
            sourceDir = baseModuleSourceDir,
            packageName = packageName,
            navMapClassName = anavExtension.navMapClassName,
            navArgsClassName = anavExtension.navArgsClassName,
            removeActivitySuffix = anavExtension.removeActivitySuffix
        )
    }

    private fun updateCompilerOptions(
        argProvider: ArgumentProvider,
        buildDir: String?,
        sourceDir: String?,
        packageName: String?,
        navMapClassName: String?,
        navArgsClassName: String?,
        removeActivitySuffix: Boolean
    ) {
        val options = mapOf(
            OPT_DEBUG to DEBUG.toString(),
            OPT_BUILD_DIR to (buildDir ?: ""),
            OPT_SOURCE_DIR to (sourceDir ?: ""),
            OPT_PACKAGE_NAME to (packageName ?: ""),
            OPT_NAV_MAP_ClASS_NAME to (navMapClassName ?: ""),
            OPT_NAV_ARGS_CLASS_NAME to (navArgsClassName ?: ""),
            OPT_REMOVE_ACTIVITY_SUFFIX to removeActivitySuffix.toString()
        )
        argProvider.options = options

        log("Anav: annotation processor options:")
        for ((k, v) in options) {
            log("[$k, $v]")
        }
    }

    private fun findAppIdFromManifest(project: Project): String? {
        val manifestFile = File(
            project.projectDir,
            "/src/main/AndroidManifest.xml"
        )
        if (!manifestFile.exists()) {
            return null
        }
        return getPackageNameFromManifest(manifestFile)
    }


    private fun Project.isRootProject(): Boolean {
        return this == rootProject
    }

    private fun Project.isAndroidProject(): Boolean {
        return plugins.hasPlugin(AndroidBasePlugin::class.java)
    }

    private fun Project.isAndroidExtAvailable(): Boolean {
        return extensions.findByType(BaseExtension::class.java) != null
    }

    private operator fun TaskContainer.get(name: String): Task? {
        return try {
            getByName(name)
        } catch (e: UnknownTaskException) {
            null
        }
    }

    private fun log(message: String) {
        if (!DEBUG) return
        println(message)
    }

    class ArgumentProvider(
        var options: Map<String, String>
    ) : CommandLineArgumentProvider {

        override fun asArguments(): MutableIterable<String> {
            return options.map { (k, v) ->
                "-A$k=$v"
            }.toMutableList()
        }
    }

    companion object {

        private var DEBUG = false

        private const val DEFAULT_PKG_SUFFIX = ".navigation"

        private const val OPT_DEBUG = "anav.debug"
        private const val OPT_BUILD_DIR = "anav.buildDir"
        private const val OPT_SOURCE_DIR = "anav.sourceDir"
        private const val OPT_PACKAGE_NAME = "anav.packageName"
        private const val OPT_NAV_MAP_ClASS_NAME = "anav.navMapClassName"
        private const val OPT_NAV_ARGS_CLASS_NAME = "anav.navArgsClassName"
        private const val OPT_REMOVE_ACTIVITY_SUFFIX = "anav.removeActivitySuffix"

        private val MSG_BASE_MODULE_NOT_FOUND = """
            'MODULE_NAME' must be a existing module in your project.
            Please check for your navConfig block:
                navConfig {
                    baseModule = 'MODULE_NAME'
                }
            If you don't need multi-modules feature, just remove this block.
        """.trimIndent()
    }
}
