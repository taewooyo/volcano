plugins {
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
  id(libs.plugins.kotlin.compose.get().pluginId)
  id(libs.plugins.jetbrains.compose.get().pluginId)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain = getByName("desktopMain")
    desktopMain.dependencies {
      implementation(project(":volcano"))
      implementation(project(":volcano-compose"))
      implementation(project(":volcano-compose-coil"))
      implementation(compose.desktop.currentOs)
    }
  }
}

compose.desktop {
  application {
    mainClass = "com.taewooyo.volcano.demo.MainKt"
  }
}
