import { useEffect, useId, useMemo, useRef, useState } from "react";
import type { KeyboardEvent } from "react";
import { computeHeatmapLayout } from "./core-adapter";
import type { HeatmapLayoutCell, HeatmapLayoutOptions, HeatmapNode } from "./types";

export interface HeatmapProps extends HeatmapLayoutOptions {
  readonly data: HeatmapNode;
  readonly className?: string;
  readonly ariaLabel?: string;
  /** Visible inset between adjacent cells, in SVG units. Defaults to 1. */
  readonly cellGap?: number;
  /** Node ID with a persistent selection outline. */
  readonly selectedId?: string | null;
  /** Layout key for an unambiguous selection when IDs repeat in different groups. */
  readonly selectedKey?: string | null;
  readonly selectedBorderColor?: string;
  readonly tooltipHoverDelayMs?: number;
  readonly logoMaxSize?: number;
  readonly metricFormatter?: (metric: number) => string;
  readonly onLeafClick?: (node: HeatmapNode, cell: HeatmapLayoutCell) => void;
  readonly onGroupClick?: (node: HeatmapNode, cell: HeatmapLayoutCell) => void;
}

function onKeyboardActivate(event: KeyboardEvent<SVGGElement>, action: () => void): void {
  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    action();
  }
}

const defaultMetricFormatter = (metric: number) => `${metric > 0 ? "+" : ""}${metric}`;

/** SVG renderer. Layout and color calculations are provided by the shared Kotlin core. */
export function Heatmap({
  data,
  width,
  height,
  groupHeaderHeight = 20,
  maximumAbsoluteMetric = 10,
  palette,
  className,
  ariaLabel = "Heatmap",
  cellGap = 1,
  selectedId = null,
  selectedKey = null,
  selectedBorderColor = "transparent",
  tooltipHoverDelayMs = 400,
  logoMaxSize = 48,
  metricFormatter = defaultMetricFormatter,
  onLeafClick,
  onGroupClick,
}: HeatmapProps) {
  if (!Number.isFinite(cellGap) || cellGap < 0) {
    throw new Error("cellGap must be a non-negative finite number.");
  }
  if (!Number.isInteger(tooltipHoverDelayMs) || tooltipHoverDelayMs < 0) {
    throw new Error("tooltipHoverDelayMs must be a non-negative integer.");
  }
  if (!Number.isFinite(logoMaxSize) || logoMaxSize < 0) {
    throw new Error("logoMaxSize must be a non-negative finite number.");
  }
  const [tooltipKey, setTooltipKey] = useState<string | null>(null);
  const logoClipPrefix = `volcano-${useId().replace(/[^a-zA-Z0-9_-]/g, "")}`;
  const tooltipTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const clearTooltipTimer = () => {
    if (tooltipTimer.current !== null) clearTimeout(tooltipTimer.current);
    tooltipTimer.current = null;
  };
  useEffect(() => clearTooltipTimer, []);
  const cells = useMemo(
    () => computeHeatmapLayout(data, { width, height, groupHeaderHeight, maximumAbsoluteMetric, palette }),
    [data, width, height, groupHeaderHeight, maximumAbsoluteMetric, palette],
  );
  const tooltipCell = tooltipKey ? cells.find((cell) => cell.key === tooltipKey && cell.isLeaf && cell.visible) : undefined;

  return (
    <svg
      width={width}
      height={height}
      viewBox={`0 0 ${width} ${height}`}
      className={className}
      role="group"
      aria-label={ariaLabel}
    >
      {cells.map((cell, index) => {
        if (!cell.visible || (index === 0 && !cell.isLeaf)) return null;
        if (!cell.isLeaf) {
          const headerHeight = Math.min(groupHeaderHeight, cell.height);
          if (headerHeight <= 0) return null;
          const activate = () => onGroupClick?.(cell.node, cell);
          return (
            <g
              key={cell.key}
              role={onGroupClick ? "button" : undefined}
              tabIndex={onGroupClick ? 0 : undefined}
              onClick={onGroupClick ? activate : undefined}
              onKeyDown={onGroupClick ? (event) => onKeyboardActivate(event, activate) : undefined}
              style={onGroupClick ? { cursor: "pointer" } : undefined}
            >
              <rect x={cell.x} y={cell.y} width={cell.width} height={headerHeight} fill="#ffffff" />
              {cell.width >= 40 && (
                <text x={cell.x + 4} y={cell.y + headerHeight / 2} fill="#252525" fontSize={12} dominantBaseline="middle">
                  {cell.node.label}
                </text>
              )}
              <title>{cell.node.label}</title>
            </g>
          );
        }

        const inset = Math.min(cellGap / 2, cell.width / 2, cell.height / 2);
        const drawWidth = Math.max(0, cell.width - inset * 2);
        const drawHeight = Math.max(0, cell.height - inset * 2);
        const activate = () => onLeafClick?.(cell.node, cell);
        const selected = selectedKey !== null ? selectedKey === cell.key : selectedId === cell.node.id;
        const minDimension = Math.min(drawWidth, drawHeight);
        const contentPadding = Math.min(6, minDimension * 0.12);
        const availableWidth = Math.max(0, drawWidth - contentPadding * 2);
        const availableHeight = Math.max(0, drawHeight - contentPadding * 2);
        const labelFontSize = Math.max(8, Math.min(26, minDimension * 0.14));
        const metricFontSize = Math.max(8, Math.min(22, minDimension * 0.12));
        const metricText = cell.node.metric === undefined ? null : metricFormatter(cell.node.metric);
        const labelFits = cell.node.label.length * labelFontSize * 0.62 <= availableWidth;
        const metricFits = metricText !== null && metricText.length * metricFontSize * 0.58 <= availableWidth;
        const showMetric = metricFits && availableHeight >= metricFontSize * 1.2;
        const showLabel = labelFits && availableHeight >= labelFontSize * 1.2 + (showMetric ? metricFontSize * 1.2 : 0);
        const logoSize = Math.min(minDimension * 0.32, logoMaxSize);
        const showLogo = Boolean(cell.node.imageUrl?.trim()) && minDimension >= 72 &&
          logoSize + 4 <= availableHeight;
        const labelHeight = showLabel ? labelFontSize * 1.2 : 0;
        const metricHeight = showMetric ? metricFontSize * 1.2 : 0;
        const logoHeight = showLogo ? logoSize + 4 : 0;
        const contentHeight = labelHeight + metricHeight + logoHeight;
        const contentTop = cell.y + inset + (drawHeight - contentHeight) / 2;
        const tooltipText = metricText === null ? cell.node.label : `${cell.node.label}  ${metricText}`;
        const beginTooltip = () => {
          clearTooltipTimer();
          tooltipTimer.current = setTimeout(() => setTooltipKey(cell.key), tooltipHoverDelayMs);
        };
        const hideTooltip = () => {
          clearTooltipTimer();
          setTooltipKey((current) => current === cell.key ? null : current);
        };
        return (
          <g
            key={cell.key}
            role={onLeafClick ? "button" : undefined}
            tabIndex={onLeafClick ? 0 : undefined}
            onClick={onLeafClick ? activate : undefined}
            onKeyDown={onLeafClick ? (event) => onKeyboardActivate(event, activate) : undefined}
            onMouseEnter={beginTooltip}
            onMouseLeave={hideTooltip}
            onFocus={beginTooltip}
            onBlur={hideTooltip}
            style={onLeafClick ? { cursor: "pointer" } : undefined}
            aria-label={tooltipText}
            aria-pressed={onLeafClick ? selected : undefined}
          >
            <rect
              x={cell.x + inset}
              y={cell.y + inset}
              width={drawWidth}
              height={drawHeight}
              fill={cell.color}
              stroke={selected && selectedBorderColor !== "transparent" ? selectedBorderColor : "#ffffff"}
              strokeWidth={selected && selectedBorderColor !== "transparent" ? 2 : 0.5}
              style={{ transition: "fill 240ms ease" }}
            />
            {showLogo && (
              <g>
                <defs>
                  <clipPath id={`${logoClipPrefix}-logo-${index}`}>
                    <circle
                      cx={cell.x + inset + drawWidth / 2}
                      cy={contentTop + logoSize / 2}
                      r={logoSize / 2}
                    />
                  </clipPath>
                </defs>
                <circle
                  cx={cell.x + inset + drawWidth / 2}
                  cy={contentTop + logoSize / 2}
                  r={logoSize / 2}
                  fill="#1e293b"
                />
                <text
                  x={cell.x + inset + drawWidth / 2}
                  y={contentTop + logoSize / 2}
                  fill="#ffffff"
                  fontSize={logoSize * 0.42}
                  fontWeight="700"
                  textAnchor="middle"
                  dominantBaseline="middle"
                  aria-hidden="true"
                >
                  {cell.node.label.slice(0, 1).toUpperCase()}
                </text>
                <image
                  href={cell.node.imageUrl}
                  x={cell.x + inset + (drawWidth - logoSize) / 2}
                  y={contentTop}
                  width={logoSize}
                  height={logoSize}
                  preserveAspectRatio="xMidYMid slice"
                  clipPath={`url(#${logoClipPrefix}-logo-${index})`}
                />
              </g>
            )}
            {showLabel && (
              <text
                x={cell.x + inset + drawWidth / 2}
                y={contentTop + logoHeight + labelHeight / 2}
                fill="#ffffff"
                fontSize={labelFontSize}
                fontWeight="700"
                textAnchor="middle"
                dominantBaseline="middle"
              >
                {cell.node.label}
              </text>
            )}
            {showMetric && (
              <text
                x={cell.x + inset + drawWidth / 2}
                y={contentTop + logoHeight + labelHeight + metricHeight / 2}
                fill="#ffffff"
                fontSize={metricFontSize}
                textAnchor="middle"
                dominantBaseline="middle"
              >
                {metricText}
              </text>
            )}
            <title>{tooltipText}</title>
          </g>
        );
      })}
      {tooltipCell && (() => {
        const metric = tooltipCell.node.metric === undefined ? null : metricFormatter(tooltipCell.node.metric);
        const text = metric === null ? tooltipCell.node.label : `${tooltipCell.node.label}  ${metric}`;
        const tooltipWidth = Math.max(76, Math.min(width - 8, text.length * 7 + 24));
        const x = Math.max(4, Math.min(width - tooltipWidth - 4, tooltipCell.x + tooltipCell.width / 2 - tooltipWidth / 2));
        const y = tooltipCell.y >= 42 ? tooltipCell.y - 36 : Math.min(height - 32, tooltipCell.y + tooltipCell.height + 6);
        return (
          <g className="heatmap-tooltip" pointerEvents="none">
            <rect x={x} y={y} width={tooltipWidth} height={28} rx={7} fill="#0f172aee" />
            <text x={x + tooltipWidth / 2} y={y + 14} fill="#ffffff" fontSize={12} textAnchor="middle" dominantBaseline="middle">
              {text}
            </text>
          </g>
        );
      })()}
    </svg>
  );
}
