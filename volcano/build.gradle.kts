import com.taewooyo.buildsrc.Configuration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("java-library")
    id("kotlin")
    id(libs.plugins.nexus.plugin.get().pluginId)
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

mavenPublishing {
    val artifactId = "volcano"
    coordinates(
        Configuration.artifactGroup,
        artifactId,
        rootProject.extra.get("libVersion").toString()
    )

    pom {
        name.set(artifactId)
        description.set("Heatmap charts created in an optimized way, fully customizable for Android. Support only Jetpack Compose.")
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}