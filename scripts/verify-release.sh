#!/usr/bin/env bash
set -euo pipefail

echo "== Volcano library verification =="
./gradlew \
  :volcano:allTests \
  :volcano-compose:allTests \
  :volcano-compose-coil:allTests \
  :benchmark:runHeatmapBenchmark \
  :androidApp:compileDebugKotlin \
  :desktopApp:compileKotlinDesktop \
  :volcano:apiCheck \
  :volcano-compose:apiCheck \
  :volcano-compose-coil:apiCheck \
  --no-daemon --console=plain

echo "== Documentation and API reference verification =="
(
  cd documentation
  npm ci
  npm run check:locales
  npm run build -- --webpack
)

echo "Release verification passed."
