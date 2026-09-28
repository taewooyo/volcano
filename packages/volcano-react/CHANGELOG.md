# React package changelog

This file tracks `@taewooyo/heatmap-react` independently from the Kotlin/Compose artifacts in the [root changelog](../../CHANGELOG.md).

## 0.3.0

- Keep the current group and selection on immutable refreshes; fall back to a surviving ancestor when removed and reset when the root ID changes.
- Add `navigationPath`, `navigateToPath`, and exact-node `drillDown` to the state hook.
- Add `ResponsiveHeatmap`, customizable `emptyContent`, and optional `valueFormatter` for accessible details/tooltips.
- Add one Tab entry point with Arrow/Home/End navigation and a visible keyboard focus indicator.
- Show a persistent selected-cell outline by default (`#0f172a`); set `transparent` to opt out.
- Respect `motion.enabled = false` for fill and press transitions, clip cell text, and cancel long presses on touch movement.
- Avoid layout invalidation for equivalent palette objects and use the optimized shared metric calculation.
- Keep live updates running during drill-down in the demo and show the selected item's latest details.
- Freeze root-relative navigation and selection paths, ignore selections for leaves outside the current tree, and include values in accessible names by default.
- Add a localized value label and clip long group titles to their headers.

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
