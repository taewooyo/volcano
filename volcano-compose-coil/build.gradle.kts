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
    name.set("volcano-compose-coil")
    description.set("Optional Coil image rendering for Volcano Compose heatmaps.")
  }
}

kotlin {
  android {
    namespace = "com.taewooyo.volcano.compose.coil"
    compileSdk = Configuration.compileSdk
    minSdk = Configuration.minSdk
  }
  jvm("desktop")
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      api(project(":volcano-compose"))
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor3)
    }
  }
}
