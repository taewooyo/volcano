import com.taewooyo.buildsrc.Configuration

rootProject.extra.apply {
    val snapshot = System.getenv("SNAPSHOT").toBoolean()
    val libVersion = if (snapshot) {
        Configuration.snapshotVersionName
    } else {
        Configuration.versionName
    }
  set("libVersion", libVersion)

  project.group = Configuration.artifactGroup
  project.version = libVersion
}
