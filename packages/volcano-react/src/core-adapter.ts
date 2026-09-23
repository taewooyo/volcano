import { com } from "volcano-volcano";
import type { HeatmapLayoutCell, HeatmapLayoutOptions, HeatmapNode } from "./types";

const RESULT_STRIDE = 5;

function cssHexToArgb(color: string): number {
  const match = /^#([0-9a-fA-F]{6})([0-9a-fA-F]{2})?$/.exec(color);
  if (!match) throw new Error(`Invalid color ${color}; expected #RRGGBB or #RRGGBBAA.`);
  const rgb = Number.parseInt(match[1], 16);
  const alpha = match[2] ? Number.parseInt(match[2], 16) : 255;
  return alpha * 0x1000000 + rgb;
}

function argbToCssHex(color: number): string {
  const argb = color >>> 0;
  const alpha = (argb >>> 24).toString(16).padStart(2, "0");
  const rgb = (argb & 0xffffff).toString(16).padStart(6, "0");
  return alpha === "ff" ? `#${rgb}` : `#${rgb}${alpha}`;
}

/** Converts ordinary TypeScript nodes to the private Kotlin/JS array boundary. */
export function computeHeatmapLayout(
  root: HeatmapNode,
  options: HeatmapLayoutOptions,
): readonly HeatmapLayoutCell[] {
  const { width, height, groupHeaderHeight = 20, maximumAbsoluteMetric = 10, palette } = options;
  if (!Number.isInteger(width) || width <= 0 || !Number.isInteger(height) || height <= 0) {
    throw new Error("width and height must be positive integers.");
  }
  if (!Number.isInteger(groupHeaderHeight) || groupHeaderHeight < 0) {
    throw new Error("groupHeaderHeight must be a non-negative integer.");
  }
  if (!Number.isFinite(maximumAbsoluteMetric) || maximumAbsoluteMetric <= 0) {
    throw new Error("maximumAbsoluteMetric must be a positive finite number.");
  }

  const nodes: HeatmapNode[] = [];
  const ids: string[] = [];
  const labels: string[] = [];
  const parentIndexes: number[] = [];
  const values: number[] = [];
  const metrics: number[] = [];
  const colors: number[] = [];
  const keys: string[] = [];
  const depths: number[] = [];
  const activeNodes = new Set<HeatmapNode>();

  function append(node: HeatmapNode, parentIndex: number, depth: number, key: string): void {
    if (activeNodes.has(node)) throw new Error("Heatmap data must not contain a cycle.");
    if (!node.id.trim()) throw new Error("Heatmap node IDs must not be blank.");
    if (!Number.isFinite(node.value)) throw new Error(`Node ${node.id} has an invalid value.`);
    if (node.metric !== undefined && !Number.isFinite(node.metric)) {
      throw new Error(`Node ${node.id} has an invalid metric.`);
    }
    activeNodes.add(node);
    const index = nodes.length;
    nodes.push(node);
    ids.push(node.id);
    labels.push(node.label);
    parentIndexes.push(parentIndex);
    values.push(node.value);
    metrics.push(node.metric ?? NaN);
    colors.push(node.color === undefined ? NaN : cssHexToArgb(node.color));
    keys.push(key);
    depths.push(depth);
    const siblingIds = new Set<string>();
    for (const child of node.children ?? []) {
      if (siblingIds.has(child.id)) throw new Error(`Duplicate sibling ID: ${child.id}.`);
      siblingIds.add(child.id);
      append(child, index, depth + 1, `${key}/${encodeURIComponent(child.id)}`);
    }
    activeNodes.delete(node);
  }

  append(root, -1, 0, encodeURIComponent(root.id));
  const argumentsForCore = [
    ids,
    labels,
    Int32Array.from(parentIndexes),
    Float64Array.from(values),
    Float64Array.from(metrics),
    Float64Array.from(colors),
    width,
    height,
    groupHeaderHeight,
    maximumAbsoluteMetric,
  ] as const;
  const raw = palette
    ? com.taewooyo.volcano.js.layoutHeatmapWithPalette(
      ...argumentsForCore,
      cssHexToArgb(palette.negative),
      cssHexToArgb(palette.neutral),
      cssHexToArgb(palette.positive),
    )
    : com.taewooyo.volcano.js.layoutHeatmap(...argumentsForCore);
  if (raw.length !== nodes.length * RESULT_STRIDE) {
    throw new Error("Kotlin/JS returned an unexpected heatmap layout size.");
  }

  return nodes.map((node, index): HeatmapLayoutCell => {
    const offset = index * RESULT_STRIDE;
    const x = raw[offset];
    const y = raw[offset + 1];
    const cellWidth = raw[offset + 2];
    const cellHeight = raw[offset + 3];
    return {
      node,
      key: keys[index],
      parentIndex: parentIndexes[index],
      depth: depths[index],
      x,
      y,
      width: cellWidth,
      height: cellHeight,
      color: argbToCssHex(raw[offset + 4]),
      isLeaf: !node.children?.length,
      visible: Number.isFinite(x) && Number.isFinite(y) && cellWidth > 0 && cellHeight > 0,
    };
  });
}
