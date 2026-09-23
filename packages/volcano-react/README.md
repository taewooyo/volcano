# @taewooyo/heatmap-react

This pre-1.0 package renders an SVG heatmap using layout and color calculations from Volcano's shared Kotlin/JS core. The package bundles that core, so React consumers do not need Kotlin or Gradle installed.

The current peer range accepts React 18.2–18.x or 19.x; the repository's development dependency is React 19.2.4. Future major versions are not declared compatible until they are tested and the package is updated.

[Integration guide](https://taewooyo.github.io/volcano/en/docs/react) · [TypeScript API reference](https://taewooyo.github.io/volcano/en/docs/react-api) · [Changelog](CHANGELOG.md) · [Web demo](https://github.com/taewooyo/volcano/tree/main/examples/react-demo)

```sh
npm install @taewooyo/heatmap-react
```

For repository development only, build the Kotlin/JS package from the repository root first:

```sh
./gradlew :volcano:jsDevelopmentLibraryCompileSync
```

Then run from this directory:

```sh
npm ci
npm test
```

Minimal usage:

```tsx
import { Heatmap } from "@taewooyo/heatmap-react";

<Heatmap
  data={{
    id: "market",
    label: "Market",
    value: 0,
    children: [
      { id: "A", label: "A", value: 60, metric: 4.2 },
      { id: "B", label: "B", value: 40, metric: -2.1 },
    ],
  }}
  width={960}
  height={600}
  metricFormatter={(metric) => `${metric > 0 ? "+" : ""}${metric.toFixed(2)}%`}
  onLeafClick={(node, cell) => console.log(node.id, cell.key)}
/>
```

`value` determines area and `metric` determines the shared red/neutral/green color. The default color range is `-10` to `+10`; set `maximumAbsoluteMetric` and `palette` to match your data. Explicit `color` accepts CSS `#RRGGBB` or `#RRGGBBAA`. Width, height, and group header height use integer pixels. The component requires explicit dimensions; apps can make it responsive by measuring their container. Use `onGroupClick` to control drill-down in your application.

The default SVG cells adapt label and metric size to the available space, animate color changes over 240 ms, and show a hover tooltip after 400 ms. These timing and rendering rules are defaults; `tooltipHoverDelayMs`, `logoMaxSize`, and `cellGap` can be configured. An optional `imageUrl` is rendered as a circular logo when a cell is large enough; the package does not fetch or cache images itself. `selectedId` selects a leaf by ID; because IDs may repeat in different groups, use `selectedKey` with the `cell.key` passed to `onLeafClick` when selection must be unambiguous. Set `ariaLabel` to describe the chart for assistive technology. `metricFormatter` controls the cell and tooltip text; it does not affect layout or color calculations.

`onLeafClick` and `onGroupClick` receive the original node and its computed layout cell. Use `onGroupClick` to control drill-down in your application. The layout cell key is a path-derived key and remains distinct when IDs repeat in separate branches.

Use `palette={{ negative: "#e53935", neutral: "#9ca3af", positive: "#16a34a" }}` to match the mobile and desktop demos. Palette colors use the same CSS hex formats as explicit node colors.

When values change, pass a new immutable `data` tree. Mutating the existing tree in place will not invalidate the component's layout memoization.

## Props

| Prop | Default | Description |
| --- | --- | --- |
| `data` | required | Immutable tree of nodes with `id`, `label`, `value`, optional `metric`, `color`, `imageUrl`, and `children`. |
| `width`, `height` | required | Positive integer dimensions in SVG pixels. |
| `groupHeaderHeight` | `20` | Group header height in pixels. |
| `maximumAbsoluteMetric` | `10` | Absolute metric value mapped to the palette's positive or negative endpoint. |
| `palette` | core default | Negative, neutral, and positive CSS hex colors. |
| `cellGap` | `1` | Inset between neighboring cells in SVG units. |
| `metricFormatter` | signed number | Formats the metric displayed in cells and tooltips. |
| `onLeafClick`, `onGroupClick` | — | Click handlers receiving `(node, layoutCell)`. |
| `selectedId`, `selectedKey` | `null` | Selected leaf by ID or unambiguous path key. `selectedKey` takes precedence. |
| `selectedBorderColor` | `transparent` | Outline color for the selected leaf. |
| `tooltipHoverDelayMs` | `400` | Hover delay before showing the tooltip. |
| `logoMaxSize` | `48` | Maximum rendered image size in SVG pixels. |
| `ariaLabel` | `Heatmap` | Accessible name for the SVG chart. |
| `className` | — | Class applied to the root SVG element. |

The hover tooltip is pointer-based, so provide `onLeafClick` or another app-level detail affordance for touch users.
