# Volcano

> A production-oriented hierarchical heatmap SDK for Kotlin Multiplatform and Compose Multiplatform.

<p align="center">
  <img src="documentation/public/images/volcano-banner.png" alt="Volcano hierarchical heatmap landscape" width="100%" />
</p>

Volcano turns immutable hierarchical data into responsive treemaps on **Android, iOS, and Desktop**. It separates area, color, navigation, image loading, and host UI ownership so the same visualization works for financial markets, service health, budgets, inventories, and any other dense changing data.

[Documentation](https://taewooyo.github.io/volcano/) · [Korean documentation](https://taewooyo.github.io/volcano/ko/docs/) · [Sample gallery](https://taewooyo.github.io/volcano/en/docs/samples) · [API reference](https://taewooyo.github.io/volcano/api-reference/index.html)

## Why Volcano

| Need | Volcano approach |
| --- | --- |
| One visualization across platforms | `commonMain` data model and Compose UI for Android, iOS, and Desktop. |
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

## Installation

Add core and Compose to `commonMain`. All target applications use the same dependencies.

```kotlin
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation("io.github.taewooyo:volcano:<version>")
      implementation("io.github.taewooyo:volcano-compose:<version>")

      // Only if the app chooses Coil for remote imageUrl values.
      implementation("io.github.taewooyo:volcano-compose-coil:<version>")
    }
  }
}
```

The base SDK never fetches an image. `imageUrl = null` or a blank URL simply renders no logo.

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

See the [platform sample gallery](https://taewooyo.github.io/volcano/en/docs/samples) for Android, Desktop, and iOS overview/drill-down captures.

## Migrating from 1.x

Volcano 2.0 deliberately replaces the former Android-only builder DSL (`root {}`, `section {}`, `element {}`), `VolcanoBuilder`, tree classes, and `Volcano()` composable. Map source data to `HeatmapNode`, create `rememberHeatmapState`, and render `Heatmap`. Keep a 1.x release pinned until each consumer completes migration; do not mix the two API generations in one integration.

## License

Copyright 2023 taewooyo

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).
