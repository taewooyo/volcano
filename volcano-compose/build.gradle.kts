import com.taewooyo.buildsrc.Configuration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.kmp.library.get().pluginId)
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlin.compose.get().pluginId)
    id(libs.plugins.jetbrains.compose.get().pluginId)
    id(libs.plugins.nexus.plugin.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

mavenPublishing {
    configure(com.vanniktech.maven.publish.KotlinMultiplatform())

    pom {
        name.set("volcano-compose")
        description.set("Compose Multiplatform UI for hierarchical Volcano heatmaps.")
    }
}

kotlin {
    android {
        namespace = "com.taewooyo.volcano.compose"
        compileSdk = Configuration.compileSdk
        minSdk = Configuration.minSdk
    }
    jvm("desktop")
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":volcano"))
            api(compose.runtime)
            api(compose.animation)
            api(compose.foundation)
            api(compose.material)
            api(compose.ui)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

}
