/*
 * Copyright (C) 2026 taewooyo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalJsExport::class)

package com.taewooyo.volcano.js

import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.SignedMetricColorScale
import com.taewooyo.volcano.squarified.SquarifiedMeasurer
import kotlin.math.min
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

private const val HEATMAP_RESULT_STRIDE = 5
private const val DEFAULT_NEGATIVE = 0xFFD44848L
private const val DEFAULT_NEUTRAL = 0xFF6B7280L
private const val DEFAULT_POSITIVE = 0xFF1E9E63L

private data class IndexedHeatmapNode(
  val sourceIndex: Int,
  val node: HeatmapNode,
  val children: List<IndexedHeatmapNode>,
)

/**
 * Measures a flat list of treemap values and returns `[x, y, width, height, ...]` for each cell.
 *
 * This deliberately small JS boundary uses only primitive arrays and numbers. It is an
 * interoperability spike, not yet the public React component API.
 */
@JsExport
public fun measureTreemap(values: DoubleArray, width: Int, height: Int): DoubleArray {
  require(width > 0) { "width must be positive." }
  require(height > 0) { "height must be positive." }
  require(values.isNotEmpty()) { "values must not be empty." }
  require(values.all { it.isFinite() && it >= 0.0 }) {
    "values must contain finite, non-negative numbers."
  }
  require(values.any { it > 0.0 }) { "values must contain at least one positive number." }

  val nodes = SquarifiedMeasurer().measureNodes(values.toList(), width, height)
  val geometry = DoubleArray(nodes.size * 4)
  nodes.forEachIndexed { index, node ->
    val offset = index * 4
    geometry[offset] = node.offsetX.toDouble()
    geometry[offset + 1] = node.offsetY.toDouble()
    geometry[offset + 2] = node.width.toDouble()
    geometry[offset + 3] = node.height.toDouble()
  }
  return geometry
}

/**
 * Lays out a hierarchical heatmap described by parallel arrays.
 *
 * `parentIndexes` uses `-1` for the single root; otherwise each parent must precede its children.
 * `metrics` and `explicitColors` use `NaN` when absent. The result has one row per input node in
 * the same order, with `[x, y, width, height, argb]` values. Coordinates are absolute within the
 * requested viewport. Group rectangles include their header area; children begin below the
 * configured header height. The root is a viewport container and has no header. Nodes without
 * visual area have `NaN` for `x` and `y`, zero size, and their resolved ARGB color.
 *
 * This array-based contract keeps Kotlin collections and data classes out of the JS API. The
 * eventual TypeScript package can convert ordinary node objects to and from this representation.
 */
@JsExport
public fun layoutHeatmap(
  ids: Array<String>,
  labels: Array<String>,
  parentIndexes: IntArray,
  values: DoubleArray,
  metrics: DoubleArray,
  explicitColors: DoubleArray,
  width: Int,
  height: Int,
  groupHeaderHeight: Int,
  maximumAbsoluteMetric: Double,
): DoubleArray = layoutHeatmapWithPalette(
  ids, labels, parentIndexes, values, metrics, explicitColors,
  width, height, groupHeaderHeight, maximumAbsoluteMetric,
  DEFAULT_NEGATIVE.toDouble(), DEFAULT_NEUTRAL.toDouble(), DEFAULT_POSITIVE.toDouble(),
)

/** Same layout contract as [layoutHeatmap], with a caller-supplied signed-metric palette. */
@JsExport
public fun layoutHeatmapWithPalette(
  ids: Array<String>,
  labels: Array<String>,
  parentIndexes: IntArray,
  values: DoubleArray,
  metrics: DoubleArray,
  explicitColors: DoubleArray,
  width: Int,
  height: Int,
  groupHeaderHeight: Int,
  maximumAbsoluteMetric: Double,
  negativeColor: Double,
  neutralColor: Double,
  positiveColor: Double,
): DoubleArray {
  val count = ids.size
  require(count > 0) { "At least one node is required." }
  require(labels.size == count && parentIndexes.size == count && values.size == count) {
    "ids, labels, parentIndexes, and values must have the same length."
  }
  require(metrics.size == count && explicitColors.size == count) {
    "metrics and explicitColors must have the same length as ids."
  }
  require(width > 0 && height > 0) { "width and height must be positive." }
  require(groupHeaderHeight >= 0) { "groupHeaderHeight must not be negative." }
  require(ids.all(String::isNotBlank)) { "Node ids must not be blank." }
  require(values.all(Double::isFinite)) { "values must contain finite numbers." }
  require(metrics.all { it.isNaN() || it.isFinite() }) {
    "metrics must be finite or NaN when absent."
  }
  require(explicitColors.all { color ->
    color.isNaN() || (color.isFinite() && color in 0.0..4_294_967_295.0 && color % 1.0 == 0.0)
  }) { "explicitColors must be unsigned ARGB integers or NaN when absent." }
  require(listOf(negativeColor, neutralColor, positiveColor).all { color ->
    color.isFinite() && color in 0.0..4_294_967_295.0 && color % 1.0 == 0.0
  }) { "Palette colors must be unsigned ARGB integers." }

  val children = Array(count) { mutableListOf<Int>() }
  var rootIndex = -1
  for (index in ids.indices) {
    val parentIndex = parentIndexes[index]
    if (parentIndex == -1) {
      require(rootIndex == -1) { "Exactly one root node is required." }
      rootIndex = index
    } else {
      require(parentIndex in 0 until index) {
        "Each parent index must refer to a node that appears earlier in the arrays."
      }
      children[parentIndex].add(index)
    }
  }
  require(rootIndex == 0) { "The root node must be the first node." }

  val nodesByIndex = arrayOfNulls<HeatmapNode>(count)
  fun buildNode(index: Int): IndexedHeatmapNode {
    val indexedChildren = children[index].map(::buildNode)
    val node = HeatmapNode(
      id = ids[index],
      label = labels[index],
      value = values[index],
      color = explicitColors[index].takeUnless(Double::isNaN)?.toLong(),
      children = indexedChildren.map(IndexedHeatmapNode::node),
      metric = metrics[index].takeUnless(Double::isNaN),
    )
    nodesByIndex[index] = node
    return IndexedHeatmapNode(index, node, indexedChildren)
  }

  val root = buildNode(rootIndex)
  val colorScale = SignedMetricColorScale(
    maximumAbsoluteMetric = maximumAbsoluteMetric,
    negative = negativeColor.toLong(),
    neutral = neutralColor.toLong(),
    positive = positiveColor.toLong(),
  )
  val result = DoubleArray(count * HEATMAP_RESULT_STRIDE)
  nodesByIndex.forEachIndexed { index, nullableNode ->
    val node = checkNotNull(nullableNode)
    val offset = index * HEATMAP_RESULT_STRIDE
    result[offset] = Double.NaN
    result[offset + 1] = Double.NaN
    result[offset + 4] = (node.color ?: colorScale.colorOf(node)).toDouble()
  }

  fun placeNode(indexedNode: IndexedHeatmapNode, x: Int, y: Int, nodeWidth: Int, nodeHeight: Int) {
    val offset = indexedNode.sourceIndex * HEATMAP_RESULT_STRIDE
    result[offset] = x.toDouble()
    result[offset + 1] = y.toDouble()
    result[offset + 2] = nodeWidth.toDouble()
    result[offset + 3] = nodeHeight.toDouble()
    val visibleChildren = indexedNode.children.filter { it.node.layoutValue > 0.0 }
    if (visibleChildren.isEmpty()) return

    val headerHeight = if (indexedNode.sourceIndex == rootIndex) {
      0
    } else {
      min(groupHeaderHeight, nodeHeight)
    }
    val contentHeight = nodeHeight - headerHeight
    if (nodeWidth <= 0 || contentHeight <= 0) {
      visibleChildren.forEach { child -> placeNode(child, x, y + headerHeight, 0, 0) }
      return
    }

    val measured = SquarifiedMeasurer().measureNodes(
      values = visibleChildren.map { it.node.layoutValue },
      width = nodeWidth,
      height = contentHeight,
    )
    visibleChildren.forEachIndexed { childIndex, child ->
      val bounds = measured[childIndex]
      placeNode(
        child,
        x + bounds.offsetX,
        y + headerHeight + bounds.offsetY,
        bounds.width,
        bounds.height,
      )
    }
  }

  placeNode(root, 0, 0, width, height)
  return result
}
