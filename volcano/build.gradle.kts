import com.taewooyo.buildsrc.Configuration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.kmp.library.get().pluginId)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.nexus.plugin.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

mavenPublishing {
    configure(com.vanniktech.maven.publish.KotlinMultiplatform())

    pom {
        name.set("volcano")
        description.set("Platform-neutral data, layout, and aggregation for hierarchical heatmaps.")
    }
}


kotlin {
    android {
        namespace = "com.taewooyo.volcano.core"
        compileSdk = Configuration.compileSdk
        minSdk = Configuration.minSdk
    }
    jvm("desktop")
    js {
        nodejs()
        binaries.library()
        generateTypeScriptDefinitions()
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

}

// Use CI's Node installation without registering a tool distribution repository in Gradle.
@Suppress("DEPRECATION_ERROR")
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().apply {
        download = false
        downloadBaseUrl = null
    }
}
