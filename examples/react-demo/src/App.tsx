import { useEffect, useMemo, useRef, useState } from "react";
import { Heatmap, useHeatmapState } from "@taewooyo/heatmap-react";
import { expandForHeatmapStressTest, marketMap, toOverview, withDemoMetrics } from "./data";
import type { DemoDataMode } from "./data";
import "./styles.css";

const palette = { negative: "#e53935", neutral: "#9ca3af", positive: "#16a34a" } as const;

function useHeatmapSize() {
  const containerRef = useRef<HTMLDivElement>(null);
  const [size, setSize] = useState({ width: 0, height: 0 });
  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;
    const observer = new ResizeObserver(([entry]) => {
      const width = Math.max(0, Math.floor(entry.contentRect.width));
      const height = Math.max(0, Math.floor(entry.contentRect.height));
      setSize((current) => current.width === width && current.height === height
        ? current
        : { width, height });
    });
    observer.observe(container);
    return () => observer.disconnect();
  }, []);
  return { containerRef, size };
}

export function App() {
  const [fastFeed, setFastFeed] = useState(false);
  const [dataMode, setDataMode] = useState<DemoDataMode>("normal");
  const [tick, setTick] = useState(0);
  const { containerRef, size } = useHeatmapSize();

  const changingMarket = useMemo(() => withDemoMetrics(marketMap, tick), [tick]);
  const displayRoot = useMemo(() => {
    if (dataMode === "normal") return changingMarket;
    const expanded = expandForHeatmapStressTest(changingMarket);
    return dataMode === "overview5k" ? toOverview(expanded) : expanded;
  }, [changingMarket, dataMode]);

  const heatmapState = useHeatmapState(displayRoot);
  const breadcrumbs = heatmapState.breadcrumbs;

  useEffect(() => {
    if (heatmapState.canNavigateUp) return;
    const timer = window.setInterval(() => setTick((current) => current + 1), fastFeed ? 100 : 2_500);
    return () => window.clearInterval(timer);
  }, [fastFeed, heatmapState.canNavigateUp]);

  const nextModeLabel = dataMode === "normal" ? "5K view" : dataMode === "overview5k" ? "5K raw" : "Normal";
  function cycleDataMode() {
    setDataMode((current) => current === "normal" ? "overview5k" : current === "overview5k" ? "raw5k" : "normal");
    heatmapState.reset();
  }

  return (
    <main className="app">
      <div className="toolbar">
        <button disabled={!heatmapState.canNavigateUp} onClick={() => heatmapState.navigateUp()}>Back</button>
        <div className="legend" aria-label="Heatmap legend">
          <span className="legend-item"><i style={{ background: palette.negative }} />−10%</span>
          <span className="legend-item"><i style={{ background: palette.neutral }} />0%</span>
          <span className="legend-item"><i style={{ background: palette.positive }} />+10%</span>
        </div>
        <button onClick={cycleDataMode}>{nextModeLabel}</button>
      </div>

      <div className="feed-row">
        <span>LIVE DEMO · sample metrics update every {fastFeed ? "100ms" : "2.5s"}</span>
        <button onClick={() => setFastFeed((current) => !current)}>{fastFeed ? "Slow feed" : "Fast feed"}</button>
      </div>

      <nav className="breadcrumbs" aria-label="Heatmap path">
        {breadcrumbs.map((node, index) => (
          <span key={`${index}-${node.id}`}>
            {index > 0 && <span className="separator">/</span>}
            <button
              disabled={index === breadcrumbs.length - 1}
              onClick={() => heatmapState.navigateToBreadcrumb(index)}
            >
              {node.label}
            </button>
          </span>
        ))}
      </nav>

      <div className="heatmap-frame" ref={containerRef}>
        {size.width > 0 && size.height > 0 && (
          <Heatmap
            data={displayRoot}
            state={heatmapState}
            width={size.width}
            height={size.height}
            groupHeaderHeight={20}
            maximumAbsoluteMetric={10}
            palette={palette}
            displayPolicy={{ metricFormatter: (metric) => `${metric > 0 ? "+" : ""}${metric.toFixed(2)}%` }}
            interaction={{ showTooltipOnHover: true, tooltipHoverDelayMillis: 400 }}
            logoMaxSize={48}
          />
        )}
      </div>
    </main>
  );
}
