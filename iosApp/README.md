# iOS sample host

`iosApp` builds the `VolcanoIosApp` framework for `iosArm64` and `iosSimulatorArm64`. The Kotlin
entry point is `mainViewController()`, which hosts the same market heatmap UI as Android and
Desktop through `ComposeUIViewController`.

The included `iosApp/VolcanoIosApp.swift` is a SwiftUI host source. Add it to an Xcode iOS App
target, add the Gradle-built `VolcanoIosApp.framework` to that target, then call the generated
`MainViewControllerKt.mainViewController()` entry point as shown.

Compile the simulator framework from the repository root:

```bash
./gradlew :iosApp:linkDebugFrameworkIosSimulatorArm64
```

An Xcode host project is included at `iosApp/XcodeHost/VolcanoIosDemo.xcodeproj`. Open it after
building the framework, select an Apple Silicon iOS Simulator, and run the `VolcanoIosDemo` scheme:

```bash
open iosApp/XcodeHost/VolcanoIosDemo.xcodeproj
```

The host project links the framework from the Gradle output directory and excludes the Intel
Simulator architecture. Re-run the Gradle command above whenever the shared Kotlin code changes.
