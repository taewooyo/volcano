# Changelog

All notable changes to Volcano are documented in this file. The project follows semantic
versioning: breaking public API changes require a new major version.

This file tracks the Kotlin/Compose artifacts. The separately versioned React npm package has its own [changelog](packages/volcano-react/CHANGELOG.md).

## [2.0.3]

- Preserve navigation and selection across immutable updates with the same root ID; reset for a different root ID.
- Add exact-node drill-down and root-relative `navigateToPath` for nested groups with repeated IDs.
- Remove quadratic membership scans during aggregation and precompute immutable node weights/metrics.
- Add `toDisplayTreeWithSources` for opening original items represented by Others. Synthetic IDs are now order-independent; do not persist or parse their old format. `maximumChildren` excludes Others.
- Show a persistent selection border by default; opt out with `Color.Transparent`.
- Add localized empty/accessible labels through `LocalHeatmapLabels`, stable Compose child keys, and make `motion.enabled = false` disable default cell color/press motion too.
- Snapshot node children and navigation path lists before caching derived values; reject selections for leaves outside the current tree.
- Include source values in Compose accessibility descriptions and detail popups, with customizable value formatting and localized Back/value labels.
- Resolve visible `Others` source paths through a prefix index and add a many-group preparation benchmark.
- Keep tooltip content tied to the current data path while live values update.

## [2.0.2]

### Changed

- Group header hover and press feedback now shades the full group, including its child cells, on pointer platforms.

## [2.0.1]

### Added

- A 240 ms Compose cell-color transition when metrics change.
- A Kotlin/JS core target for sharing layout and color calculations with web integrations.

## [2.0.0] - 2026-09-22

### Added

- Kotlin Multiplatform core targeting Android, iOS, and Desktop JVM.
- Compose Multiplatform `Heatmap` with drill-down, breadcrumbs, selection, accessibility, and
  adaptive cell content.
- Domain-neutral `value` and signed `metric` model, color scales, sorting, filtering, and display
  aggregation.
- Optional `volcano-compose-coil` integration for remote logos.
- ABI compatibility baselines, desktop benchmark, platform demos, documentation site, and Dokka
  API reference generation.

### Changed

- Project structure is now `volcano`, `volcano-compose`, and optional integration artifacts.

### Removed

- 1.x `root {}`, `section {}`, and `element {}` builder DSL.
- 1.x tree model, `VolcanoBuilder`, and `Volcano()` composable.

## [1.x]

The Android-only DSL release line is retained only for applications that have not yet migrated.
See [the 2.0 migration guide](documentation/content/docs/migration.mdx).
