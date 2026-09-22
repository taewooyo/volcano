plugins {
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
  id(libs.plugins.kotlin.compose.get().pluginId)
  id(libs.plugins.jetbrains.compose.get().pluginId)
}

kotlin {
  iosArm64 {
    binaries.framework {
      baseName = "VolcanoIosApp"
      isStatic = true
    }
  }
  iosSimulatorArm64 {
    binaries.framework {
      baseName = "VolcanoIosApp"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":volcano"))
      implementation(project(":volcano-compose"))
      implementation(project(":volcano-compose-coil"))
    }

    // UIKit entry points are shared by every iOS architecture.
    val iosMain = create("iosMain")
    iosMain.dependsOn(getByName("commonMain"))
    getByName("iosArm64Main").dependsOn(iosMain)
    getByName("iosSimulatorArm64Main").dependsOn(iosMain)
  }
}
