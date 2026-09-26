/** Visual defaults used by [Heatmap]. Values use CSS colors and SVG pixels. */
export interface HeatmapStyle {
  readonly borderColor?: string;
  readonly groupHeaderColor?: string;
  readonly groupHeaderTextColor?: string;
  readonly selectedBorderColor?: string;
  readonly leafTextColor?: string;
}

/** Rules for fitting leaf content. Values use SVG pixels. */
export interface HeatmapDisplayPolicy {
  readonly hideContentBelow?: number;
  readonly showMetricAbove?: number;
  readonly showLabelAbove?: number;
  readonly cellContentPadding?: number;
  readonly adaptiveContent?: boolean;
  readonly metricFormatter?: (metric: number) => string;
}

/** Pointer interaction settings supported by the SVG renderer. */
export interface HeatmapInteraction {
  readonly drillDownOnGroupClick?: boolean;
  readonly selectLeafOnClick?: boolean;
  readonly showTooltipOnLongClick?: boolean;
  readonly tooltipDurationMillis?: number;
  readonly showTooltipOnHover?: boolean;
  readonly tooltipHoverDelayMillis?: number;
}

/** Transition settings shared with the Compose renderer where SVG supports them. */
export interface HeatmapMotion {
  readonly enabled?: boolean;
  readonly durationMillis?: number;
  readonly initialScale?: number;
  readonly pressScale?: number;
  readonly pressedAlpha?: number;
  readonly pressDurationMillis?: number;
}
