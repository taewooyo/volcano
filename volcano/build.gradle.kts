@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-library")
    id("kotlin")
    `maven-publish`
}

afterEvaluate {
    publishing {
        publications {
            register("kotlin", MavenPublication::class) {
                from(components["kotlin"])
                groupId = "com.taewooyo.volcano"
                artifactId = "volcano"
                version = "0.0.1"
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}