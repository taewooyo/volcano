# Changelog

All notable changes to Volcano are documented in this file. The project follows semantic
versioning: breaking public API changes require a new major version.

## [2.0.0] - Unreleased

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
