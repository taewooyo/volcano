import com.taewooyo.buildsrc.Configuration

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
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
        description.set("Modernized and sophisticated tooltips, fully customizable with an arrow and animations for Android.")
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}