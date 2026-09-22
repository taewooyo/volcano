package com.taewooyo.buildsrc

object Configuration {
    const val compileSdk = 37
    const val targetSdk = 35
    const val minSdk = 23
    const val majorVersion = 2
    const val minorVersion = 0
    const val patchVersion = 0
    const val versionName = "$majorVersion.$minorVersion.$patchVersion"
    const val versionCode = 4
    const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
    const val artifactGroup = "io.github.taewooyo"
}
