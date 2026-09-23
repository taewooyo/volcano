# TypeScript core smoke test

This private example checks that the generated Kotlin/JS core is usable as a local npm dependency from TypeScript. It is not the public React package.

From the repository root, build the JavaScript library and its generated type declarations:

```sh
./gradlew :volcano:jsDevelopmentLibraryCompileSync
```

Then, from this directory:

```sh
npm install
npm test
```

The generated package is at `build/js/packages/volcano-volcano`. The test checks both TypeScript compilation and runtime calls to `measureTreemap` and `layoutHeatmap`. Run the Gradle command again after changing the Kotlin core.
