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

import com.taewooyo.volcano.heatmap.HeatmapAggregation
import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.HeatmapSort
import com.taewooyo.volcano.heatmap.aggregateSmallChildren
import com.taewooyo.volcano.heatmap.toDisplayTreeWithSources
import kotlin.system.measureNanoTime

private var preparationSink: Any? = null

private fun sample(name: String, block: () -> Any) {
  repeat(10) { preparationSink = block() }
  val times = List(25) {
    measureNanoTime { preparationSink = block() } / 1_000_000.0
  }.sorted()
  println("$name median=${times[12]}ms p95=${times[22]}ms")
}

/** Data preparation only; does not measure UI frames. */
public fun main() {
  for (count in listOf(1_000, 5_000, 10_000, 20_000)) {
    val root = HeatmapNode(
      id = "root",
      label = "Root",
      value = 0.0,
      children = List(count) { index ->
        HeatmapNode("leaf-$index", "Leaf $index", (index % 100 + 1).toDouble(), metric = 1.0)
      },
    )
    sample("aggregate-default-flat-$count") {
      root.aggregateSmallChildren(HeatmapAggregation())
    }
    sample("aggregate-top12-flat-$count") {
      root.aggregateSmallChildren(HeatmapAggregation(maximumChildren = 12))
    }
  }
  for (depth in listOf(20, 100, 300)) {
    var node = HeatmapNode("leaf", "Leaf", 1.0, metric = 1.0)
    repeat(depth) { index ->
      node = HeatmapNode("g$index", "Group", 0.0, children = listOf(node))
    }
    val nodes = mutableListOf<HeatmapNode>()
    var current = node
    while (true) {
      nodes += current
      if (current.isLeaf) break
      current = current.children.first()
    }
    sample("all-effectiveMetrics-chain-$depth") { nodes.map { it.effectiveMetric } }
  }
  for (groupCount in listOf(100, 500, 1_000)) {
    val root = HeatmapNode(
      id = "root",
      label = "Root",
      value = 0.0,
      children = List(groupCount) { index ->
        HeatmapNode(
          id = "group-$index",
          label = "Group $index",
          value = 0.0,
          children = listOf(
            HeatmapNode("large-$index", "Large $index", 9_999.0),
            HeatmapNode("small-$index", "Small $index", 1.0),
          ),
        )
      },
    )
    val aggregation = HeatmapAggregation(minimumChildFraction = 0.9 / groupCount)
    sample("display-tree-with-sources-$groupCount-groups") {
      root.toDisplayTreeWithSources(HeatmapSort.NONE, aggregation)
    }
  }
}
