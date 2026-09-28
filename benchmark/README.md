# Volcano benchmark

Run the repeatable desktop-JVM layout baseline from the repository root:

```bash
./gradlew :benchmark:runHeatmapBenchmark
```

The benchmark uses a deterministic, two-level tree with 20 groups and measures only the shared
`SquarifiedMeasurer` recursion at a 1080x1920 viewport. It reports median and p95 values after
warmup for 100, 500, 1,000, and 5,000 leaf nodes.

This is intentionally not a UI frame benchmark. Compose composition, rendering, and input
latency depend on the target platform and must be profiled separately on Android, Desktop, and
iOS before replacing the normal cell renderer with Canvas or Modifier Node implementations.

## Data preparation and React output

```bash
./gradlew :benchmark:runHeatmapPreparationBenchmark
npm --prefix packages/volcano-react run benchmark
```

Build the Kotlin/JS core first with `./gradlew :volcano:jsDevelopmentLibraryCompileSync`.
The preparation benchmark uses 10 warmups and 25 samples. The React benchmark measures the
adapter (15 warmups / 40 samples), production SSR (3 / 12), and deep chains (3 / 12).
Data creation is excluded except for the adapter's internal Kotlin-node reconstruction.
SSR includes calculation and string generation, not DOM commit, paint, memory use or FPS.
There are no hardware-dependent timing assertions in CI.

Local macOS arm64 observations for this change (milliseconds, medians):

| Workload | Before | Earlier after run | Latest rerun |
| --- | ---: | ---: | ---: |
| JVM default aggregation, 20,000 siblings | 421.25 | 1.57 | 1.41 |
| Kotlin/JS adapter, chain depth 300 | 83.57 | 0.18 | 0.17 |
| Kotlin/JS adapter, 5,000 leaves / 20 groups | 4.89 | 3.38 | 3.36 |
| React production SSR, 5,000 leaves / 20 groups | 25.32 | 31.27 | 24.61 |

These are local development-machine measurements, not device guarantees. The latest rerun used
the benchmark's 3 warmups and 12 samples; its 5,000-cell SSR median was 24.61 ms (p95 27.99 ms),
with 2,450,364 bytes of SVG markup. An earlier paired run measured 31.27 ms after the change
against 25.32 ms before it, but that increase did not reproduce in two consecutive latest runs.
Treat the earlier SSR regression as inconclusive benchmark noise until a controlled browser run
shows otherwise. This benchmark measures layout and server-side string generation, not DOM commit,
paint, memory use, or FPS. Node-derived values are prepared at construction, so cached getter
timings alone are not end-to-end speedups; the Kotlin/JS adapter measurement includes preparation.
The keyboard and focus work improves interaction after rendering, while large-cell UI cost still
needs profiling in real browsers.
