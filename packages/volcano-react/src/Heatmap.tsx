import { useEffect, useId, useMemo, useRef, useState } from "react";
import type { KeyboardEvent, ReactNode } from "react";
import type { HeatmapDisplayPolicy, HeatmapInteraction, HeatmapMotion, HeatmapStyle } from "./configuration";
import { computeHeatmapLayout } from "./core-adapter";
import type { HeatmapState } from "./HeatmapState";
import type { HeatmapLayoutCell, HeatmapLayoutOptions, HeatmapNode } from "./types";

export interface HeatmapProps extends HeatmapLayoutOptions {
  readonly data: HeatmapNode;
  readonly state?: HeatmapState;
  readonly displayPolicy?: HeatmapDisplayPolicy;
  readonly style?: HeatmapStyle;
  readonly interaction?: HeatmapInteraction;
  readonly motion?: HeatmapMotion;
  readonly className?: string;
  readonly ariaLabel?: string;
  /** Content shown when no positive visual values are available. */
  readonly emptyContent?: ReactNode;
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
  /** Optional area-value formatter, included in cell descriptions and tooltips. */
  readonly valueFormatter?: (value: number) => string;
  /** Accessible label paired with the formatted area value. */
  readonly valueLabel?: string;
  readonly onLeafClick?: (node: HeatmapNode, cell: HeatmapLayoutCell) => void;
  readonly onLeafLongClick?: (node: HeatmapNode, cell: HeatmapLayoutCell) => void;
  readonly onGroupClick?: (node: HeatmapNode, cell: HeatmapLayoutCell) => void;
  readonly onLeafHover?: (node: HeatmapNode | null) => void;
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
  state,
  displayPolicy,
  style,
  interaction,
  motion,
  width,
  height,
  groupHeaderHeight = 20,
  maximumAbsoluteMetric = 10,
  palette,
  className,
  ariaLabel = "Heatmap",
  emptyContent = "No data",
  cellGap = 1,
  selectedId = null,
  selectedKey = null,
  selectedBorderColor = "#0f172a",
  tooltipHoverDelayMs = 400,
  logoMaxSize = 48,
  metricFormatter = defaultMetricFormatter,
  valueFormatter,
  valueLabel = "Value",
  onLeafClick,
  onLeafLongClick,
  onGroupClick,
  onLeafHover,
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
  const svgRef = useRef<SVGSVGElement>(null);
  const focusWithin = useRef(false);
  const [focusedKey, setFocusedKey] = useState<string | null>(null);
  const [tooltipKey, setTooltipKey] = useState<string | null>(null);
  const [hoveredGroupKey, setHoveredGroupKey] = useState<string | null>(null);
  const [pressedGroupKey, setPressedGroupKey] = useState<string | null>(null);
  const [pressedLeafKey, setPressedLeafKey] = useState<string | null>(null);
  const [levelEntered, setLevelEntered] = useState(false);
  const longPressTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const tooltipDurationTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const longPressTriggered = useRef(false);
  const resolvedStyle = {
    borderColor: style?.borderColor ?? "#ffffff",
    groupHeaderColor: style?.groupHeaderColor ?? "#ffffff",
    groupHeaderTextColor: style?.groupHeaderTextColor ?? "#252525",
    selectedBorderColor: style?.selectedBorderColor ?? selectedBorderColor,
    leafTextColor: style?.leafTextColor ?? "#ffffff",
  };
  const resolvedDisplayPolicy = {
    hideContentBelow: displayPolicy?.hideContentBelow ?? 16,
    showMetricAbove: displayPolicy?.showMetricAbove ?? 40,
    showLabelAbove: displayPolicy?.showLabelAbove ?? 48,
    cellContentPadding: displayPolicy?.cellContentPadding ?? 6,
    adaptiveContent: displayPolicy?.adaptiveContent ?? true,
    metricFormatter: displayPolicy?.metricFormatter ?? metricFormatter,
  };
  const resolvedInteraction = {
    drillDownOnGroupClick: interaction?.drillDownOnGroupClick ?? true,
    selectLeafOnClick: interaction?.selectLeafOnClick ?? true,
    showTooltipOnLongClick: interaction?.showTooltipOnLongClick ?? false,
    tooltipDurationMillis: interaction?.tooltipDurationMillis ?? 2_000,
    showTooltipOnHover: interaction?.showTooltipOnHover ?? false,
    tooltipHoverDelayMillis: interaction?.tooltipHoverDelayMillis ?? tooltipHoverDelayMs,
  };
  const resolvedMotion = {
    enabled: motion?.enabled ?? true,
    durationMillis: motion?.durationMillis ?? 180,
    initialScale: motion?.initialScale ?? 0.98,
    pressScale: motion?.pressScale ?? 0.97,
    pressedAlpha: motion?.pressedAlpha ?? 0.92,
    pressDurationMillis: motion?.pressDurationMillis ?? 90,
  };
  if (resolvedInteraction.tooltipHoverDelayMillis < 0 || !Number.isInteger(resolvedInteraction.tooltipHoverDelayMillis)) {
    throw new Error("interaction.tooltipHoverDelayMillis must be a non-negative integer.");
  }
  if (resolvedMotion.pressDurationMillis < 0 || !Number.isInteger(resolvedMotion.pressDurationMillis)) {
    throw new Error("motion.pressDurationMillis must be a non-negative integer.");
  }
  if (resolvedMotion.durationMillis < 0 || !Number.isInteger(resolvedMotion.durationMillis)) {
    throw new Error("motion.durationMillis must be a non-negative integer.");
  }
  if (!(resolvedMotion.initialScale > 0 && resolvedMotion.initialScale <= 1)) {
    throw new Error("motion.initialScale must be in the range (0, 1].");
  }
  if (resolvedInteraction.tooltipDurationMillis < 0 || !Number.isInteger(resolvedInteraction.tooltipDurationMillis)) {
    throw new Error("interaction.tooltipDurationMillis must be a non-negative integer.");
  }
  if (!(resolvedMotion.pressScale > 0 && resolvedMotion.pressScale <= 1)) {
    throw new Error("motion.pressScale must be in the range (0, 1].");
  }
  if (!(resolvedMotion.pressedAlpha > 0 && resolvedMotion.pressedAlpha <= 1)) {
    throw new Error("motion.pressedAlpha must be in the range (0, 1].");
  }
  const logoClipPrefix = `volcano-${useId().replace(/[^a-zA-Z0-9_-]/g, "")}`;
  const tooltipTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const clearTooltipTimer = () => {
    if (tooltipTimer.current !== null) clearTimeout(tooltipTimer.current);
    tooltipTimer.current = null;
  };
  useEffect(() => () => {
    clearTooltipTimer();
    if (longPressTimer.current !== null) clearTimeout(longPressTimer.current);
    if (tooltipDurationTimer.current !== null) clearTimeout(tooltipDurationTimer.current);
  }, []);
  const navigationKey = JSON.stringify(state?.navigationPath ?? []);
  useEffect(() => {
    if (!resolvedMotion.enabled) {
      setLevelEntered(true);
      return;
    }
    setLevelEntered(false);
    const frame = requestAnimationFrame(() => setLevelEntered(true));
    return () => cancelAnimationFrame(frame);
  }, [navigationKey, resolvedMotion.enabled, resolvedMotion.durationMillis]);
  const stablePalette = useMemo(() => palette ? { ...palette } : undefined, [palette?.negative, palette?.neutral, palette?.positive]);
  const visibleData = state?.visibleNode ?? data;
  const cells = useMemo(
    () => computeHeatmapLayout(visibleData, { width, height, groupHeaderHeight, maximumAbsoluteMetric, palette: stablePalette }),
    [visibleData, width, height, groupHeaderHeight, maximumAbsoluteMetric, stablePalette],
  );
  const currentCells = useRef(cells);
  const currentLongClick = useRef(onLeafLongClick);
  useEffect(() => { currentCells.current = cells; currentLongClick.current = onLeafLongClick; }, [cells, onLeafLongClick]);
  const tooltipCell = tooltipKey ? cells.find((cell) => cell.key === tooltipKey && cell.isLeaf && cell.visible) : undefined;

  const interactiveCells = cells.filter((cell, index) => cell.visible && !(index === 0 && !cell.isLeaf) &&
    (cell.isLeaf ? Boolean(onLeafClick || onLeafLongClick || state) : groupHeaderHeight > 0 && Boolean(onGroupClick || state)));
  const activeKey = interactiveCells.some((cell) => cell.key === focusedKey) ? focusedKey : interactiveCells[0]?.key;
  useEffect(() => {
    setTooltipKey(null);
    setHoveredGroupKey(null);
    setPressedGroupKey(null);
    setPressedLeafKey(null);
    clearTooltipTimer();
    if (longPressTimer.current !== null) clearTimeout(longPressTimer.current);
    if (tooltipDurationTimer.current !== null) clearTimeout(tooltipDurationTimer.current);
    longPressTriggered.current = false;
  }, [visibleData.id, navigationKey]);
  useEffect(() => {
    if (focusWithin.current) {
      svgRef.current?.querySelector<SVGGElement>('[data-heatmap-key][tabindex="0"]')?.focus();
    }
  }, [activeKey, navigationKey, visibleData.id]);
  const handleChartKeyDown = (event: KeyboardEvent<SVGSVGElement>) => {
    if (!["ArrowRight", "ArrowDown", "ArrowLeft", "ArrowUp", "Home", "End"].includes(event.key)) return;
    const targets = Array.from(svgRef.current?.querySelectorAll<SVGGElement>("[data-heatmap-key]") ?? []);
    if (!targets.length) return;
    const current = targets.indexOf(event.target as SVGGElement);
    const next = event.key === "Home" ? 0 : event.key === "End" ? targets.length - 1 :
      Math.max(0, Math.min(targets.length - 1, current + (["ArrowLeft", "ArrowUp"].includes(event.key) ? -1 : 1)));
    event.preventDefault();
    targets[next].focus();
  };

  return (
    <svg
      ref={svgRef}
      onKeyDown={handleChartKeyDown}
      onFocusCapture={(event) => {
        focusWithin.current = true;
        const key = (event.target as Element).getAttribute("data-heatmap-key");
        if (key) setFocusedKey(key);
      }}
      onBlurCapture={(event) => {
        if (event.relatedTarget && !event.currentTarget.contains(event.relatedTarget as Node)) focusWithin.current = false;
      }}
      width={width}
      height={height}
      viewBox={`0 0 ${width} ${height}`}
      className={["volcano-heatmap", className].filter(Boolean).join(" ")}
      role="group"
      aria-label={ariaLabel}
    >
      <style>{`.volcano-heatmap [data-heatmap-key]:focus-visible > rect:first-of-type { stroke: #ffffff; stroke-width: 3; filter: drop-shadow(0 0 1px #000000); }`}</style>
      <g
        style={resolvedMotion.enabled ? {
          opacity: levelEntered ? 1 : 0,
          transform: `scale(${levelEntered ? 1 : resolvedMotion.initialScale})`,
          transformBox: "view-box",
          transformOrigin: "center",
          transition: `opacity ${resolvedMotion.durationMillis}ms ease, transform ${resolvedMotion.durationMillis}ms ease`,
        } : undefined}
      >
      <rect width={width} height={height} fill={resolvedStyle.borderColor} pointerEvents="none" />
      {cells.map((cell, index) => {
        if (!cell.visible || (index === 0 && !cell.isLeaf)) return null;
        if (!cell.isLeaf) {
          const headerHeight = Math.min(groupHeaderHeight, cell.height);
          if (headerHeight <= 0) return null;
          const groupInteractive = onGroupClick !== undefined || state !== undefined;
          const activate = () => {
            onGroupClick?.(cell.node, cell);
            if (resolvedInteraction.drillDownOnGroupClick) state?.drillDown(cell.node);
          };
          return (
            <g
              key={cell.key}
              role={groupInteractive ? "button" : undefined}
              data-heatmap-key={groupInteractive ? cell.key : undefined}
              tabIndex={groupInteractive ? (cell.key === activeKey ? 0 : -1) : undefined}
              onClick={groupInteractive ? activate : undefined}
              onKeyDown={groupInteractive ? (event) => onKeyboardActivate(event, activate) : undefined}
              onMouseEnter={groupInteractive ? () => setHoveredGroupKey(cell.key) : undefined}
              onMouseLeave={groupInteractive ? () => {
                setHoveredGroupKey((current) => current === cell.key ? null : current);
                setPressedGroupKey((current) => current === cell.key ? null : current);
              } : undefined}
              onMouseDown={groupInteractive ? () => setPressedGroupKey(cell.key) : undefined}
              onMouseUp={groupInteractive ? () => setPressedGroupKey((current) => current === cell.key ? null : current) : undefined}
              style={groupInteractive ? { cursor: "pointer", outline: "none" } : undefined}
            >
              <rect x={cell.x} y={cell.y} width={cell.width} height={headerHeight} fill={resolvedStyle.groupHeaderColor} />
              {cell.width >= 40 && (
                <>
                  <defs>
                    <clipPath id={`${logoClipPrefix}-group-${index}`} clipPathUnits="userSpaceOnUse">
                      <rect x={cell.x + 4} y={cell.y} width={Math.max(0, cell.width - 8)} height={headerHeight} />
                    </clipPath>
                  </defs>
                  <text clipPath={`url(#${logoClipPrefix}-group-${index})`} x={cell.x + 4} y={cell.y + headerHeight / 2} fill={resolvedStyle.groupHeaderTextColor} fontSize={12} dominantBaseline="middle">
                    {cell.node.label}
                  </text>
                </>
              )}
              <title>{cell.node.label}</title>
            </g>
          );
        }

        const inset = Math.min(cellGap / 2, cell.width / 2, cell.height / 2);
        const drawWidth = Math.max(0, cell.width - inset * 2);
        const drawHeight = Math.max(0, cell.height - inset * 2);
        const leafInteractive = onLeafClick !== undefined || onLeafLongClick !== undefined || state !== undefined;
        const activate = () => {
          if (resolvedInteraction.selectLeafOnClick) state?.select(cell.node);
          onLeafClick?.(cell.node, cell);
        };
        const selected = state ? state.isSelected(cell.node) : selectedKey !== null ? selectedKey === cell.key : selectedId === cell.node.id;
        const pressed = pressedLeafKey === cell.key;
        const minDimension = Math.min(drawWidth, drawHeight);
        const contentPadding = Math.min(resolvedDisplayPolicy.cellContentPadding, minDimension * 0.12);
        const availableWidth = Math.max(0, drawWidth - contentPadding * 2);
        const availableHeight = Math.max(0, drawHeight - contentPadding * 2);
        const labelFontSize = Math.max(8, Math.min(26, minDimension * 0.14));
        const metricFontSize = Math.max(8, Math.min(22, minDimension * 0.12));
        const metricText = cell.node.metric === undefined ? null : resolvedDisplayPolicy.metricFormatter(cell.node.metric);
        const labelFits = cell.node.label.length * labelFontSize * 0.62 <= availableWidth;
        const metricFits = metricText !== null && metricText.length * metricFontSize * 0.58 <= availableWidth;
        const showMetric = minDimension >= resolvedDisplayPolicy.hideContentBelow && metricFits &&
          (resolvedDisplayPolicy.adaptiveContent ? availableHeight >= metricFontSize * 1.2 : minDimension >= resolvedDisplayPolicy.showMetricAbove);
        const showLabel = minDimension >= resolvedDisplayPolicy.hideContentBelow && labelFits &&
          (resolvedDisplayPolicy.adaptiveContent
            ? availableHeight >= labelFontSize * 1.2 + (showMetric ? metricFontSize * 1.2 : 0)
            : minDimension >= resolvedDisplayPolicy.showLabelAbove);
        const logoSize = Math.min(minDimension * 0.32, logoMaxSize);
        const showLogo = Boolean(cell.node.imageUrl?.trim()) && minDimension >= 72 &&
          logoSize + 4 <= availableHeight;
        const labelHeight = showLabel ? labelFontSize * 1.2 : 0;
        const metricHeight = showMetric ? metricFontSize * 1.2 : 0;
        const logoHeight = showLogo ? logoSize + 4 : 0;
        const contentHeight = labelHeight + metricHeight + logoHeight;
        const contentTop = cell.y + inset + (drawHeight - contentHeight) / 2;
        const formattedValue = valueFormatter?.(cell.node.value) ?? String(cell.node.value);
        const tooltipText = [cell.node.label, `${valueLabel}: ${formattedValue}`, metricText].filter((part) => part != null).join("  ");
        const beginTooltip = () => {
          clearTooltipTimer();
          onLeafHover?.(cell.node);
          if (!resolvedInteraction.showTooltipOnHover) return;
          tooltipTimer.current = setTimeout(() => setTooltipKey(cell.key), resolvedInteraction.tooltipHoverDelayMillis);
        };
        const hideTooltip = () => {
          clearTooltipTimer();
          if (longPressTimer.current !== null) clearTimeout(longPressTimer.current);
          longPressTimer.current = null;
          setTooltipKey((current) => current === cell.key ? null : current);
          onLeafHover?.(null);
        };
        return (
          <g
            key={cell.key}
            role={leafInteractive ? "button" : undefined}
            data-heatmap-key={leafInteractive ? cell.key : undefined}
            tabIndex={leafInteractive ? (cell.key === activeKey ? 0 : -1) : undefined}
            onClick={leafInteractive ? () => {
              if (longPressTriggered.current) {
                longPressTriggered.current = false;
                return;
              }
              activate();
            } : undefined}
            onKeyDown={leafInteractive ? (event) => onKeyboardActivate(event, activate) : undefined}
            onMouseEnter={beginTooltip}
            onMouseLeave={() => {
              hideTooltip();
              setPressedLeafKey((current) => current === cell.key ? null : current);
            }}
            onMouseDown={leafInteractive ? () => setPressedLeafKey(cell.key) : undefined}
            onMouseUp={leafInteractive ? () => setPressedLeafKey((current) => current === cell.key ? null : current) : undefined}
            onTouchStart={leafInteractive && (resolvedInteraction.showTooltipOnLongClick || onLeafLongClick !== undefined) ? () => {
              longPressTriggered.current = false;
              longPressTimer.current = setTimeout(() => {
                const latestCell = currentCells.current.find((candidate) => candidate.key === cell.key && candidate.visible);
                if (!latestCell) return;
                longPressTriggered.current = true;
                currentLongClick.current?.(latestCell.node, latestCell);
                if (resolvedInteraction.showTooltipOnLongClick) {
                  setTooltipKey(cell.key);
                  if (resolvedInteraction.tooltipDurationMillis > 0) {
                    tooltipDurationTimer.current = setTimeout(() => setTooltipKey((current) => current === cell.key ? null : current), resolvedInteraction.tooltipDurationMillis);
                  }
                }
              }, 500);
            } : undefined}
            onTouchMove={() => {
              if (longPressTimer.current !== null) clearTimeout(longPressTimer.current);
              longPressTimer.current = null;
            }}
            onTouchEnd={() => {
              if (longPressTimer.current !== null) clearTimeout(longPressTimer.current);
              longPressTimer.current = null;
            }}
            onTouchCancel={() => {
              if (longPressTimer.current !== null) clearTimeout(longPressTimer.current);
              longPressTimer.current = null;
              longPressTriggered.current = false;
            }}
            onFocus={beginTooltip}
            onBlur={hideTooltip}
            style={leafInteractive ? {
              cursor: "pointer",
              outline: "none",
              transformBox: "fill-box",
              transformOrigin: "center",
              transform: resolvedMotion.enabled && pressed ? `scale(${resolvedMotion.pressScale})` : "scale(1)",
              opacity: resolvedMotion.enabled && pressed ? resolvedMotion.pressedAlpha : 1,
              transition: resolvedMotion.enabled ? `transform ${resolvedMotion.pressDurationMillis}ms ease, opacity ${resolvedMotion.pressDurationMillis}ms ease` : undefined,
            } : undefined}
            aria-label={tooltipText}
            aria-pressed={leafInteractive ? selected : undefined}
          >
            <rect
              x={cell.x + inset}
              y={cell.y + inset}
              width={drawWidth}
              height={drawHeight}
              fill={cell.color}
              stroke={selected && resolvedStyle.selectedBorderColor !== "transparent" ? resolvedStyle.selectedBorderColor : resolvedStyle.borderColor}
              strokeWidth={selected && resolvedStyle.selectedBorderColor !== "transparent" ? 2 : 0.5}
              style={resolvedMotion.enabled ? { transition: "fill 240ms ease" } : undefined}
            />
            {(showLabel || showMetric) && (
              <defs><clipPath id={`${logoClipPrefix}-text-${index}`}>
                <rect x={cell.x + inset + contentPadding} y={cell.y + inset} width={availableWidth} height={drawHeight} />
              </clipPath></defs>
            )}
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
                clipPath={`url(#${logoClipPrefix}-text-${index})`}
                x={cell.x + inset + drawWidth / 2}
                y={contentTop + logoHeight + labelHeight / 2}
                fill={resolvedStyle.leafTextColor}
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
                clipPath={`url(#${logoClipPrefix}-text-${index})`}
                x={cell.x + inset + drawWidth / 2}
                y={contentTop + logoHeight + labelHeight + metricHeight / 2}
                fill={resolvedStyle.leafTextColor}
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
      {cells.map((cell, index) => {
        if (!cell.visible || (index === 0 && !cell.isLeaf) || cell.isLeaf) return null;
        if (cell.key !== hoveredGroupKey && cell.key !== pressedGroupKey) return null;
        return (
          <rect
            key={`${cell.key}-group-interaction-overlay`}
            x={cell.x}
            y={cell.y}
            width={cell.width}
            height={cell.height}
            fill="#000000"
            fillOpacity={0.08}
            pointerEvents="none"
          />
        );
      })}
      {tooltipCell && (() => {
        const metric = tooltipCell.node.metric === undefined ? null : resolvedDisplayPolicy.metricFormatter(tooltipCell.node.metric);
        const formattedValue = valueFormatter?.(tooltipCell.node.value) ?? String(tooltipCell.node.value);
        const text = [tooltipCell.node.label, `${valueLabel}: ${formattedValue}`, metric].filter((part) => part != null).join("  ");
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
      </g>
      {!cells.some((cell) => cell.visible && cell.isLeaf) && (
        <foreignObject width={width} height={height}>
          <div role="status" style={{ display: "grid", placeItems: "center", width: "100%", height: "100%" }}>{emptyContent}</div>
        </foreignObject>
      )}
    </svg>
  );
}
