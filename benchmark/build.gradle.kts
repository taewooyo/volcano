plugins {
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain = getByName("desktopMain")
    desktopMain.dependencies {
      implementation(project(":volcano"))
    }
  }
}

tasks.register<JavaExec>("runHeatmapBenchmark") {
  group = "verification"
  description = "Measures hierarchical heatmap layout performance on the desktop JVM."
  dependsOn("desktopMainClasses")
  classpath = files(layout.buildDirectory.dir("classes/kotlin/desktop/main")) +
    configurations.named("desktopRuntimeClasspath").get()
  mainClass.set("com.taewooyo.volcano.benchmark.MainKt")
}
