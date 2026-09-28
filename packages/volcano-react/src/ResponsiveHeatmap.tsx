import { useEffect, useRef, useState } from "react";
import type { CSSProperties, ReactNode } from "react";
import { Heatmap } from "./Heatmap";
import type { HeatmapProps } from "./Heatmap";

export interface ResponsiveHeatmapProps extends Omit<HeatmapProps, "width" | "height"> {
  /** The parent must supply a bounded height; for example { height: 360 }. */
  readonly containerStyle?: CSSProperties;
  readonly containerClassName?: string;
  readonly fallback?: ReactNode;
}

/** Measures its container and waits while hidden or unmeasured, including during SSR. */
export function ResponsiveHeatmap({ containerStyle, containerClassName, fallback = null, ...props }: ResponsiveHeatmapProps) {
  const container = useRef<HTMLDivElement>(null);
  const [size, setSize] = useState({ width: 0, height: 0 });
  useEffect(() => {
    const element = container.current;
    if (!element) return;
    const update = (width: number, height: number) => {
      const next = { width: Math.max(0, Math.floor(width)), height: Math.max(0, Math.floor(height)) };
      setSize((current) => current.width === next.width && current.height === next.height ? current : next);
    };
    update(element.clientWidth, element.clientHeight);
    if (typeof ResizeObserver === "undefined") {
      const measure = () => update(element.clientWidth, element.clientHeight);
      window.addEventListener("resize", measure);
      return () => window.removeEventListener("resize", measure);
    }
    const observer = new ResizeObserver(([entry]) => {
      if (entry) update(entry.contentRect.width, entry.contentRect.height);
    });
    observer.observe(element);
    return () => observer.disconnect();
  }, []);
  return (
    <div ref={container} className={containerClassName} style={{ width: "100%", height: "100%", minWidth: 0, overflow: "hidden", ...containerStyle }}>
      {size.width > 0 && size.height > 0 ? <Heatmap {...props} {...size} /> : fallback}
    </div>
  );
}
