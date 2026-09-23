# React market heatmap demo

This demo uses the same nine-stock Desktop/iOS fixture, 100 ms / 2.5 s metric-update formula, and Normal → 5K view → 5K raw mode cycle. It uses the React package's Kotlin/JS layout and palette, circular logos, adaptive cell text, 400 ms hover tooltips, and 240 ms color transitions.

Build the local packages first:

```sh
./gradlew :volcano:jsDevelopmentLibraryCompileSync
cd packages/volcano-react && npm ci && npm run build
cd ../../examples/react-demo && npm ci && npm run dev
```

Open the URL printed by Vite. Use **Fast feed** to switch from 2.5-second updates to 100-millisecond updates. Use **5K view** and **5K raw** to inspect aggregated and full 5,000-leaf modes. Clicking a group header drills down; **Back** navigates up. The feed pauses while drilled in, matching the Desktop/iOS demos.

5K raw mode is included as a manual stress scenario. Frame-rate and browser-version results have not yet been recorded as a release benchmark.
