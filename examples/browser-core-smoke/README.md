# Browser core smoke test

This private Vite app verifies that a browser bundler can import the generated Kotlin/JS package and call both exported APIs. It does not render a React heatmap.

From the repository root:

```sh
./gradlew :volcano:jsDevelopmentLibraryCompileSync
```

From this directory:

```sh
npm ci
npm run build
npm run dev
```

Open the local URL printed by Vite. The page should display `PASS`. Rebuild the Kotlin/JS package after changing the Kotlin core.
