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
