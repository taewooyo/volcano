# React package changelog

This file tracks `@taewooyo/heatmap-react` independently from the Kotlin/Compose artifacts in the [root changelog](../../CHANGELOG.md).

## 0.2.0

- Added Compose-aligned `style`, `displayPolicy`, `interaction`, and `motion` configuration objects.
- Added `useHeatmapState` for drill-down, breadcrumbs, and leaf selection.
- Added group-wide hover/press feedback, touch long-press tooltip support, and configurable press/navigation motion.
- Hover tooltips now default to off to match Compose; enable them with `interaction.showTooltipOnHover`.

## 0.1.0 — Initial public release

- React SVG `Heatmap` with a TypeScript data model and a bundled Kotlin/JS layout and color core.
- Public `computeHeatmapLayout` function and layout-cell types.
- Adaptive labels, optional round logos, hover/focus tooltips, and a 240 ms fill-color transition.
- Leaf/group callbacks, keyboard activation, selection by ID or path key, configurable palette and metric formatting.
- Interactive web demo with a fast feed and aggregated/raw 5,000-leaf modes.

This is a pre-1.0 API. Review release notes and the [React API reference](https://taewooyo.github.io/volcano/en/docs/react-api) before upgrading.
