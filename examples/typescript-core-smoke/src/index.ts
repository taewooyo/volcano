import { com } from "volcano-volcano";

const api = com.taewooyo.volcano.js;

const geometry = api.measureTreemap(new Float64Array([6, 3, 1]), 300, 200);
const expected = [0, 0, 180, 200, 180, 0, 120, 150, 180, 150, 120, 50];
if (geometry.length !== expected.length || expected.some((value, index) => geometry[index] !== value)) {
  throw new Error(`Unexpected treemap geometry: ${Array.from(geometry)}`);
}

const heatmap = api.layoutHeatmap(
  ["root", "first", "second"],
  ["Root", "First", "Second"],
  new Int32Array([-1, 0, 0]),
  new Float64Array([3, 2, 1]),
  new Float64Array([NaN, 0.05, -0.03]),
  new Float64Array([NaN, NaN, NaN]),
  300,
  200,
  0,
  0.1,
);
if (heatmap.length !== 15 || heatmap[0] !== 0 || heatmap[1] !== 0 || heatmap[2] !== 300 || heatmap[3] !== 200) {
  throw new Error(`Unexpected heatmap layout: ${Array.from(heatmap)}`);
}

console.log("Kotlin/JS core imported and called from TypeScript successfully.");
