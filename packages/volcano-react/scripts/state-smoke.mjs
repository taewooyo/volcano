import assert from "node:assert/strict";
import { createElement, useState } from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { Heatmap, ResponsiveHeatmap, useHeatmapState } from "../dist/index.js";

const leaf = { id: "leaf", label: "Before", value: 1 };
const nested = { id: "same", label: "Nested", value: 0, children: [leaf] };
const other = { id: "other", label: "Other", value: 1 };
const outer = { id: "same", label: "Outer", value: 0, children: [nested, other] };
const initial = { id: "root", label: "Root", value: 0, children: [outer] };
const updatedLeaf = { ...leaf, label: "After", value: 9 };
const updated = { ...initial, children: [{ ...outer, children: [{ ...nested, children: [updatedLeaf] }, other] }] };

// React render-phase updates exercise the real hook without pretending to test browser input.
function Lifecycle() {
  const [root, setRoot] = useState(initial);
  const [phase, setPhase] = useState(0);
  const state = useHeatmapState(root);
  if (phase === 0) {
    assert.equal(state.drillDown(nested), true);
    state.select(leaf);
    setPhase(1);
  } else if (phase === 1) {
    assert.deepEqual(state.navigationPath, ["same", "same"]);
    assert.equal(state.selectedNode, leaf);
    setRoot(updated);
    setPhase(2);
  } else if (phase === 2) {
    assert.deepEqual(state.navigationPath, ["same", "same"]);
    assert.equal(state.selectedNode, updatedLeaf);
    assert.equal(Object.isFrozen(state.navigationPath), true);
    assert.equal(Object.isFrozen(state.selectionPath), true);
    state.select({ id: "outside", label: "Outside", value: 100 });
    assert.equal(state.selectedNode, updatedLeaf);
    assert.equal(state.isSelected(leaf), false);
    assert.equal(state.navigateToPath(["missing"]), false);
    assert.equal(state.navigateToPath(["same", "same", "leaf"]), false);
    assert.equal(state.navigateToBreadcrumb(0.5), false);
    setRoot({ ...initial, children: [{ ...outer, children: [other] }] });
    setPhase(3);
  } else if (phase === 3) {
    assert.deepEqual(state.navigationPath, ["same"]);
    assert.equal(state.selectedNode, null);
    setRoot({ ...updated, id: "another-root" });
    setPhase(4);
  } else {
    assert.deepEqual(state.navigationPath, []);
    assert.equal(state.canNavigateUp, false);
    return createElement("span", null, "state checks passed");
  }
  return null;
}
assert.match(renderToStaticMarkup(createElement(Lifecycle)), /state checks passed/);
const html = renderToStaticMarkup(createElement(Heatmap, {
  data: initial, width: 500, height: 300, onLeafClick() {}, onGroupClick() {},
  motion: { enabled: false }, selectedId: "leaf", valueFormatter: (value) => `${value} units`,
}));
assert.equal((html.match(/tabindex="0"/g) ?? []).length, 1);
assert.ok(html.includes('tabindex="-1"'));
assert.ok(html.includes('stroke="#0f172a"'));
assert.ok(html.includes("1 units"));
assert.ok(html.includes('aria-label="Before  Value: 1 units"'));
assert.ok(!html.includes("fill 240ms"));
assert.ok(!html.includes("transition:"));
const empty = renderToStaticMarkup(createElement(Heatmap, {
  data: { id: "empty", label: "Empty", value: 0 }, width: 200, height: 100,
  emptyContent: "표시할 데이터가 없습니다",
}));
assert.match(empty, /표시할 데이터가 없습니다/);
assert.match(empty, /role="status"/);
assert.equal((empty.match(/data-heatmap-key=/g) ?? []).length, 0);
assert.match(renderToStaticMarkup(createElement(ResponsiveHeatmap, { data: initial, fallback: "Measuring" })), /Measuring/);
console.log("Navigation refresh, duplicate paths, deletion, selection, empty content, motion and responsive SSR checks passed.");

function InlineData() {
  const [phase, setPhase] = useState(0);
  const state = useHeatmapState({ ...initial });
  if (phase === 0) {
    state.drillDown("same");
    setPhase(1);
  }
  return createElement("span", null, state.visibleNode.label);
}
assert.match(renderToStaticMarkup(createElement(InlineData)), /Outer/);
