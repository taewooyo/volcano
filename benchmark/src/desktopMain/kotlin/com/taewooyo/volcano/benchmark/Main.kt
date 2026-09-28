/*
 * Copyright (C) 2023 taewooyo
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
package com.taewooyo.volcano.benchmark

import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.squarified.SquarifiedMeasurer
import kotlin.system.measureNanoTime

private const val WARMUP_ITERATIONS = 20
private const val MEASURED_ITERATIONS = 50
private const val VIEWPORT_WIDTH = 1_080
private const val VIEWPORT_HEIGHT = 1_920

/**
 * Repeatable JVM baseline for the platform-independent hierarchical layout engine.
 *
 * This intentionally measures layout calculation only. Compose composition, draw, and frame
 * timing require platform-specific instrumentation and are measured after this baseline.
 */
public fun main() {
  println("Volcano hierarchical layout benchmark")
  println(
    "viewport=${VIEWPORT_WIDTH}x$VIEWPORT_HEIGHT, warmup=$WARMUP_ITERATIONS, samples=$MEASURED_ITERATIONS",
  )
  println("| leaves | median ms | p95 ms | layouts / run |")
  println("| ---: | ---: | ---: | ---: |")

  listOf(100, 500, 1_000, 5_000).forEach { leafCount ->
    val root = createTree(leafCount)
    repeat(WARMUP_ITERATIONS) { layoutTree(root, VIEWPORT_WIDTH, VIEWPORT_HEIGHT) }
    val samples = List(MEASURED_ITERATIONS) {
      var layouts = 0
      val elapsed = measureNanoTime { layouts = layoutTree(root, VIEWPORT_WIDTH, VIEWPORT_HEIGHT) }
      elapsed to layouts
    }
    val sortedNanos = samples.map { it.first }.sorted()
    val median = sortedNanos[sortedNanos.size / 2] / 1_000_000.0
    val p95 = sortedNanos[((sortedNanos.size - 1) * 0.95).toInt()] / 1_000_000.0
    println("| $leafCount | ${format(median)} | ${format(p95)} | ${samples.first().second} |")
  }
}

private fun createTree(leafCount: Int): HeatmapNode {
  val groupCount = minOf(20, leafCount)
  val groups = (0 until groupCount).map { groupIndex ->
    val start = leafCount * groupIndex / groupCount
    val end = leafCount * (groupIndex + 1) / groupCount
    val leaves = (start until end).map { leafIndex ->
      HeatmapNode(
        id = "leaf-$leafIndex",
        label = "Leaf $leafIndex",
        value = deterministicWeight(leafIndex),
        metric = deterministicMetric(leafIndex),
      )
    }
    HeatmapNode(
      id = "group-$groupIndex",
      label = "Group $groupIndex",
      value = leaves.sumOf(HeatmapNode::layoutValue),
      children = leaves,
    )
  }
  return HeatmapNode(
    id = "root",
    label = "Root",
    value = groups.sumOf(HeatmapNode::layoutValue),
    children = groups,
  )
}

private fun deterministicWeight(index: Int): Double =
  ((index * 1_103L + 97L) % 10_000L + 1L).toDouble()

private fun deterministicMetric(index: Int): Double = ((index * 313L) % 2_001L - 1_000L) / 100.0

private fun layoutTree(node: HeatmapNode, width: Int, height: Int): Int {
  val children = node.children.filter { it.layoutValue > 0.0 }
  if (children.isEmpty() || width <= 0 || height <= 0) return 0
  val rectangles = SquarifiedMeasurer().measureNodes(
    values = children.map(HeatmapNode::layoutValue),
    width = width,
    height = height,
  )
  return 1 + children.indices.sumOf { index ->
    val rectangle = rectangles[index]
    layoutTree(children[index], rectangle.width, rectangle.height)
  }
}

private fun format(value: Double): String = value.toString().take(6)
