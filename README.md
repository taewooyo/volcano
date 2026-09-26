# Volcano

<p align="center">
  <a href="https://github.com/taewooyo/volcano/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/taewooyo/volcano/build.yml?label=build" alt="Build status" /></a>
  <a href="https://github.com/taewooyo/volcano/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="Apache 2.0 license" /></a>
  <a href="https://kotlinlang.org/docs/multiplatform.html"><img src="https://img.shields.io/badge/Kotlin%20Multiplatform-Android%20%7C%20iOS%20%7C%20Desktop-7f52ff" alt="Kotlin Multiplatform" /></a>
  <a href="https://taewooyo.github.io/volcano/en/docs/getting-started"><img src="https://img.shields.io/badge/docs-online-e85d04" alt="Documentation" /></a>
</p>

> A hierarchical heatmap SDK for Kotlin Multiplatform, Compose Multiplatform, and React web.

Volcano turns dense, changing data into an adaptive treemap that users can scan, select, and drill
into. It is finance-friendly, but the data model is domain-neutral: use it for markets, service
health, budgets, inventories, capacity, or any other hierarchy with measurable values.

<p align="center">
  <img src="documentation/public/images/volcano-banner.png" alt="Volcano hierarchical heatmap landscape" width="100%" />
</p>

[Documentation](https://taewooyo.github.io/volcano/) · [Korean documentation](https://taewooyo.github.io/volcano/ko/docs/) · [React guide](https://taewooyo.github.io/volcano/en/docs/react) · [React API](https://taewooyo.github.io/volcano/en/docs/react-api) · [Kotlin API](https://taewooyo.github.io/volcano/api-reference/index.html) · [Sample gallery](https://taewooyo.github.io/volcano/en/docs/samples)

## At a glance

| Property | Details |
| --- | --- |
| Platforms | Android, iOS, and Desktop through Compose Multiplatform; React web through an SVG package backed by Kotlin/JS calculations |
| Rendering | Squarified treemap layout with adaptive content and measured cell padding |
| Navigation | Overview, group drill-down, breadcrumbs, selection, and parent navigation |
| Images | Nullable image URLs with an optional Coil integration; no forced network stack |
| Scale | Aggregation helpers plus a 5,000-leaf layout benchmark |

## Why Volcano

| Need | Volcano approach |
| --- | --- |
| One calculation core across platforms | `commonMain` layout and color rules; Compose UI for Android/iOS/Desktop and a separate React SVG UI for web. |
| Dense information without unreadable cells | Measured adaptive content hides logo, label, and metric progressively. |
| Overview and detail without separate screens | Group headers drill down; breadcrumbs and Back return to the parent group. |
| A library, not a finance-only widget | `value` controls area, while a domain-neutral signed `metric` controls color. |
| No forced network stack | Image URLs are nullable; Coil support is an optional artifact. |
| Production-scale data | Aggregate first, drill down on demand, and verify raw 5,000-leaf behavior with the benchmark. |

## Modules

| Module | Use it for |
| --- | --- |
| `volcano` | Immutable model, sorting, filtering, aggregation, color contracts, and squarified layout. |
| `volcano-compose` | Compose `Heatmap`, state, navigation, default cells, accessibility, and interaction. |
| `volcano-compose-coil` | Optional `CoilHeatmapLogo` implementation for remote `imageUrl` values. |
| `@taewooyo/heatmap-react` | npm package with TypeScript API, React SVG renderer, and bundled Kotlin/JS core. |

The three Gradle artifacts use the same version. Most Compose applications need `volcano` and
`volcano-compose`; add Coil only when remote logo loading is desired. The React npm package is
versioned separately (`0.2.0`) and does not require Gradle in consuming apps.

## Compose installation

Add core and Compose to `commonMain`. All target applications use the same dependencies.

Use `2.0.2` for the three Kotlin/Compose artifacts. This release adds group-wide hover and press feedback. Keep all three Gradle artifacts on the same version.

```kotlin
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation("io.github.taewooyo:volcano:2.0.2")
      implementation("io.github.taewooyo:volcano-compose:2.0.2")

      // Only if the app chooses Coil for remote imageUrl values.
      implementation("io.github.taewooyo:volcano-compose-coil:2.0.2")
    }
  }
}
```

The base SDK never fetches an image. `imageUrl = null` or a blank URL simply renders no logo.

## React web installation

```bash
npm install @taewooyo/heatmap-react
```

React 18.2–18.x and 19.x consumers import the TypeScript API directly. The npm package bundles the shared
Kotlin/JS layout and color core, so consumers do not install Kotlin or Gradle. React renders SVG;
it does not embed the Compose UI. Version `0.2.0` adds Compose-aligned configuration and the `useHeatmapState` hook.

```tsx
import { Heatmap, type HeatmapNode } from "@taewooyo/heatmap-react";

const market: HeatmapNode = {
  id: "market", label: "Market", value: 0,
  children: [
    { id: "A", label: "Alpha", value: 60, metric: 4.2 },
    { id: "B", label: "Beta", value: 40, metric: -2.1 },
  ],
};

<Heatmap data={market} width={960} height={480} ariaLabel="Market performance" />;
```

The host provides positive integer dimensions and a new immutable tree when values change. Use
`useHeatmapState` for drill-down/navigation state, or manage it in the host. `value` determines area and `metric` determines signed color. See the
[React integration guide](https://taewooyo.github.io/volcano/en/docs/react) and
[React API reference](https://taewooyo.github.io/volcano/en/docs/react-api) for callbacks, selection,
colors, and accessibility.

### Version catalog

If the project uses `libs.versions.toml`, define the version and libraries once:

```toml
[versions]
volcano = "2.0.2"

[libraries]
volcano-core = { module = "io.github.taewooyo:volcano", version.ref = "volcano" }
volcano-compose = { module = "io.github.taewooyo:volcano-compose", version.ref = "volcano" }
volcano-compose-coil = { module = "io.github.taewooyo:volcano-compose-coil", version.ref = "volcano" }
```

Then use the aliases from `commonMain`:

```kotlin
commonMain.dependencies {
  implementation(libs.volcano.core)
  implementation(libs.volcano.compose)
  // Optional:
  implementation(libs.volcano.compose.coil)
}
```

## Five-minute integration

Create an immutable tree outside composition. Each sibling `id` must be non-blank and unique; only positive finite `value` values receive visual area.

```kotlin
val market = HeatmapNode(
  id = "market",
  label = "US Market",
  value = 100.0,
  children = listOf(
    HeatmapNode(
      id = "technology",
      label = "Technology",
      value = 42.0,
      children = listOf(
        HeatmapNode(
          id = "nvidia",
          label = "NVIDIA",
          value = 12.0,       // rectangle area
          metric = 3.2,       // signed color value
          imageUrl = "https://cdn.example.com/logos/nvidia.png",
        ),
      ),
    ),
  ),
)
```

Render it in a bounded Compose area. Volcano does not impose a width or height.

```kotlin
val state = rememberHeatmapState(market)
val scale = SignedMetricColorScale(maximumAbsoluteMetric = 10.0)

Column(Modifier.fillMaxSize()) {
  HeatmapBreadcrumb(state)
  Heatmap(
    state = state,
    modifier = Modifier.weight(1f).fillMaxWidth(),
    colorScale = scale,
    interaction = HeatmapInteraction(
      drillDownOnGroupClick = true,
      selectLeafOnClick = true,
    ),
    logoContent = { node, size -> CoilHeatmapLogo(node, size) },
    onLeafClick = { node -> openDetail(node.id) },
  )
  HeatmapLegend(entries = scale.legendEntries())
}
```

Use the Coil slot only after adding `volcano-compose-coil`; otherwise omit `logoContent` or provide an implementation for your own image stack.

## Interaction model

| Input | Default behavior | Host responsibility |
| --- | --- | --- |
| Group header tap | Drill into that group | Record analytics or intercept with `onGroupClick`. |
| Leaf tap | Select the leaf | Open a detail destination with `onLeafClick`. |
| Android Back / toolbar Back | `state.navigateUp()` while possible | Install system/host Back dispatch. |
| Desktop hover | Optional tooltip after 400ms | Enable `showTooltipOnHover` or use `onLeafHover`. |
| Long press | Optional tooltip for 2 seconds | Enable it or present a host-owned sheet/dialog. |

`HeatmapState` exposes `visibleNode`, `breadcrumbs`, `selectedNode`, `selectedId`, and `canNavigateUp`. Call `clearSelection()` when the host closes a detail panel.

## Responsive by design

`HeatmapDisplayPolicy` uses actual measured space rather than a single device-specific threshold. It removes secondary content before labels, and labels before values only when a rectangle cannot render them cleanly. Padding also scales down for small cells. This prevents a 5,000-item phone overview from turning into an unreadable grid.

For deliberately deterministic visual systems, provide thresholds explicitly:

```kotlin
val policy = HeatmapDisplayPolicy(
  adaptiveContent = true,
  hideContentBelow = 16.dp,
  showMetricAbove = 40.dp,
  showLabelAbove = 48.dp,
  cellContentPadding = 6.dp,
)
```

## Large datasets

Do not expose every leaf in an initial mobile viewport simply because it can be laid out. Build a compact display tree in a ViewModel or repository, then use drill-down, filtering, or search to reveal raw data.

```kotlin
val overview = rawMarket.toDisplayTree(
  aggregation = HeatmapAggregation(
    maximumChildren = 12,
    minimumChildFraction = 0.01,
    othersLabel = "Others",
  ),
)
Heatmap(state = rememberHeatmapState(overview))
```

The `benchmark` module exercises the shared layout engine with 5,000 leaves. Treat it as a layout measurement, not a universal end-user frame-time guarantee: profile actual labels, images, devices, and animation policy on each target.

## Platform notes

- **Android:** apply edge-to-edge and safe drawing padding in the host; connect `BackHandler` only while `state.canNavigateUp` is true.
- **iOS:** host the shared `ComposeUIViewController` in UIKit or SwiftUI; set `CADisableMinimumFrameDurationOnPhone = YES` for high-refresh-rate devices.
- **Desktop:** keep the window resizable; use adaptive content and optionally enable pointer hover tooltips.
- **React web:** measure the container and pass `width`/`height`; use `useHeatmapState` for breadcrumbs, Back, and selection, or manage them in app state. The SVG renderer transitions changed fill colors over 240 ms by default.

## Accessibility

Default cells expose label, formatted metric, and selection state. Group headers, breadcrumbs, and Back controls expose navigation semantics. If `cellContent` replaces a default cell, preserve an equivalent accessible label and domain meaning. Do not communicate positive/negative or healthy/unhealthy state through color alone.

## Samples and verification

| Module | Command |
| --- | --- |
| Android demo | `./gradlew :androidApp:installDebug` |
| Desktop demo | `./gradlew :desktopApp:run` |
| iOS Simulator framework | `./gradlew :iosApp:linkDebugFrameworkIosSimulatorArm64` |
| Core and Compose tests | `./gradlew :volcano:allTests :volcano-compose:allTests` |
| Public API compatibility | `./gradlew :volcano:apiCheck :volcano-compose:apiCheck :volcano-compose-coil:apiCheck` |
| React package | `./gradlew :volcano:jsDevelopmentLibraryCompileSync`, then `cd packages/volcano-react && npm ci && npm test` |
| React web demo | Build the React package, then `cd examples/react-demo && npm ci && npm run dev` |

Run the complete Linux release gate locally with:

```bash
./scripts/verify-release.sh
```

This also checks the Korean/English documentation set, Dokka API output, static documentation build, and the 5,000-leaf layout benchmark. The GitHub `Release verification` workflow runs the same gate and separately links the iOS Simulator framework on macOS. React package checks are separate from this Kotlin release gate.

See the [sample gallery](https://taewooyo.github.io/volcano/en/docs/samples) for Android, Desktop, and iOS captures and the React demo instructions.

## Migrating from 1.x

Volcano 2.0 deliberately replaces the former Android-only builder DSL (`root {}`, `section {}`, `element {}`), `VolcanoBuilder`, tree classes, and `Volcano()` composable. Map source data to `HeatmapNode`, create `rememberHeatmapState`, and render `Heatmap`. Keep a 1.x release pinned until each consumer completes migration; do not mix the two API generations in one integration.

## License

Copyright 2023 taewooyo

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).
