import { createRequire } from "node:module";
import { createElement } from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { Heatmap, computeHeatmapLayout } from "../dist/index.js";

const data = {
  id: "root",
  label: "Market",
  value: 0,
  children: [
    { id: "up", label: "Up", value: 6, metric: 10, imageUrl: "https://example.test/up.png" },
    { id: "down", label: "Down", value: 4, metric: -10, color: "#11223380" },
  ],
};

const cells = computeHeatmapLayout(data, { width: 300, height: 200, groupHeaderHeight: 0 });
if (cells.length !== 3 || cells[1].color !== "#1e9e63" || cells[2].color !== "#11223380") {
  throw new Error(`Unexpected layout cells: ${JSON.stringify(cells)}`);
}
if (cells[0].width !== 300 || cells[0].height !== 200 || !cells[1].visible || !cells[2].visible) {
  throw new Error("Unexpected heatmap geometry.");
}
const paletteCells = computeHeatmapLayout(data, {
  width: 300,
  height: 200,
  groupHeaderHeight: 0,
  palette: { negative: "#e53935", neutral: "#9ca3af", positive: "#16a34a" },
});
if (paletteCells[1].color !== "#16a34a" || paletteCells[2].color !== "#11223380") {
  throw new Error("Custom palette or explicit-color precedence is incorrect.");
}

const nested = computeHeatmapLayout({
  id: "root",
  label: "Root",
  value: 0,
  children: [
    { id: "left", label: "Left", value: 0, children: [{ id: "same", label: "A", value: 2 }] },
    { id: "right", label: "Right", value: 0, children: [{ id: "same", label: "B", value: 3 }] },
  ],
}, { width: 300, height: 200 });
if (nested.length !== 5 || nested[2].key === nested[4].key || nested[2].parentIndex !== 1 || nested[4].parentIndex !== 3) {
  throw new Error("Nested nodes were not mapped to unique paths and parents.");
}

for (const invalidData of [
  { id: "root", label: "Root", value: 1, color: "red" },
  { id: "root", label: "Root", value: 0, children: [{ id: "same", label: "A", value: 1 }, { id: "same", label: "B", value: 1 }] },
]) {
  let rejected = false;
  try { computeHeatmapLayout(invalidData, { width: 300, height: 200 }); } catch { rejected = true; }
  if (!rejected) throw new Error("Invalid heatmap data was accepted.");
}

const html = renderToStaticMarkup(createElement(Heatmap, {
  data,
  width: 300,
  height: 200,
  groupHeaderHeight: 0,
  ariaLabel: "Portfolio heatmap",
}));
if (!html.includes("<svg") || !html.includes('fill="#1e9e63"') || !html.includes('fill="#11223380"')) {
  throw new Error(`Unexpected SVG output: ${html}`);
}
if (!html.includes("<clipPath") || !html.includes('clip-path="url(#') || !html.includes("https://example.test/up.png")) {
  throw new Error("Logo images are not rendered with the circular SVG crop.");
}
if (!html.includes(">U</text>") || !html.includes("transition:fill 240ms ease") || !html.includes('aria-label="Portfolio heatmap"')) {
  throw new Error("Logo fallback or metric color transition is missing.");
}
let formattedMetricCount = 0;
const interactiveHtml = renderToStaticMarkup(createElement(Heatmap, {
  data,
  width: 300,
  height: 200,
  groupHeaderHeight: 0,
  selectedId: "down",
  selectedBorderColor: "#123456",
  metricFormatter: (metric) => {
    formattedMetricCount += 1;
    return `${metric > 0 ? "+" : ""}${metric.toFixed(2)}%`;
  },
}));
if (!interactiveHtml.includes('stroke="#123456"') || !interactiveHtml.includes("+10.00%") || formattedMetricCount !== 2) {
  throw new Error("Selected-cell styling or formatted metrics are missing.");
}

const duplicateIdData = {
  id: "root",
  label: "Root",
  value: 0,
  children: [
    { id: "left", label: "Left", value: 0, children: [{ id: "same", label: "First", value: 1 }] },
    { id: "right", label: "Right", value: 0, children: [{ id: "same", label: "Second", value: 1 }] },
  ],
};
const duplicateCells = computeHeatmapLayout(duplicateIdData, { width: 300, height: 200 });
const secondDuplicateKey = duplicateCells.find((cell) => cell.node.label === "Second").key;
const keyedSelectionHtml = renderToStaticMarkup(createElement(Heatmap, {
  data: duplicateIdData,
  width: 300,
  height: 200,
  selectedKey: secondDuplicateKey,
  selectedBorderColor: "#abcdef",
}));
if ((keyedSelectionHtml.match(/stroke="#abcdef"/g) ?? []).length !== 1) {
  throw new Error("Path-based selection did not distinguish repeated node IDs.");
}

const configuredHtml = renderToStaticMarkup(createElement(Heatmap, {
  data: {
    id: "root",
    label: "Root",
    value: 0,
    children: [{ id: "group", label: "Group", value: 0, children: [{ id: "leaf", label: "Leaf", value: 1 }] }],
  },
  width: 300,
  height: 200,
  groupHeaderHeight: 20,
  style: { borderColor: "#010203", groupHeaderColor: "#040506", leafTextColor: "#070809" },
  displayPolicy: { adaptiveContent: false, showLabelAbove: 1, showMetricAbove: 1 },
  interaction: { showTooltipOnHover: false },
  motion: { durationMillis: 120, initialScale: 0.95, pressScale: 0.95, pressedAlpha: 0.8, pressDurationMillis: 70 },
  interaction: { showTooltipOnLongClick: true, tooltipDurationMillis: 800, showTooltipOnHover: false },
}));
if (!configuredHtml.includes('fill="#010203"') || !configuredHtml.includes('fill="#040506"') || !configuredHtml.includes('fill="#070809"')) {
  throw new Error("Compose-aligned React configuration was not applied.");
}

const require = createRequire(import.meta.url);
const commonJs = require("../dist/index.cjs");
if (typeof commonJs.Heatmap !== "function" || typeof commonJs.computeHeatmapLayout !== "function") {
  throw new Error("CommonJS exports are missing.");
}

console.log("React package ESM, CommonJS, shared layout, SVG cell, and circular-logo smoke checks passed.");
