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
package com.taewooyo.volcano.heatmap

/** Ordering applied independently to every visible group in a heatmap tree. */
public enum class HeatmapSort {
  NONE,
  LAYOUT_VALUE_DESCENDING,
  LABEL_ASCENDING,
}

/** Options for reducing dense groups into a readable display tree. */
public data class HeatmapAggregation(
  val minimumChildFraction: Double = 0.0,
  /** Maximum retained children, excluding the additional Others cell. */
  val maximumChildren: Int? = null,
  val othersLabel: String = "Others",
) {

  init {
    require(minimumChildFraction in 0.0..1.0) {
      "minimumChildFraction must be between 0.0 and 1.0."
    }
    require(maximumChildren == null || maximumChildren > 0) {
      "maximumChildren must be positive when set."
    }
    require(othersLabel.isNotBlank()) { "othersLabel must not be blank." }
  }
}

/** Returns a recursively sorted copy of this display tree. */
public fun HeatmapNode.sorted(sort: HeatmapSort): HeatmapNode = copy(
  children = children.map { it.sorted(sort) }.let { nodes ->
    when (sort) {
      HeatmapSort.NONE -> nodes
      HeatmapSort.LAYOUT_VALUE_DESCENDING -> nodes.sortedByDescending(HeatmapNode::layoutValue)
      HeatmapSort.LABEL_ASCENDING -> nodes.sortedBy { it.label }
    }
  },
)

/**
 * Removes leaf instruments rejected by [predicate], pruning empty groups on the way back up.
 * A caller can keep the original root when this function returns `null`.
 */
public fun HeatmapNode.filterLeaves(predicate: (HeatmapNode) -> Boolean): HeatmapNode? {
  if (isLeaf) return takeIf(predicate)
  val filteredChildren = children.mapNotNull { it.filterLeaves(predicate) }
  return copy(children = filteredChildren).takeIf { filteredChildren.isNotEmpty() }
}

/**
 * Recursively combines small direct children into one leaf named [HeatmapAggregation.othersLabel].
 *
 * The generated leaf contains the summed layout value and weighted metric of omitted children.
 * It is intentionally a leaf so callers can render a compact overview without another nested
 * drill-down level.
 */
public fun HeatmapNode.aggregateSmallChildren(aggregation: HeatmapAggregation): HeatmapNode = copy(
  children = aggregateChildren(
    children.map {
      it.aggregateSmallChildren(aggregation)
    },
    aggregation,
  ),
)

private fun aggregateChildren(
  children: List<HeatmapNode>,
  aggregation: HeatmapAggregation,
  onAggregated: (HeatmapNode, List<HeatmapNode>) -> Unit = { _, _ -> },
): List<HeatmapNode> {
  if (children.isEmpty()) return children
  val totalValue = children.sumOf(HeatmapNode::layoutValue)
  if (totalValue <= 0.0) return children

  val fractionQualified = children.filter { child ->
    child.layoutValue / totalValue >= aggregation.minimumChildFraction
  }
  val retainedIds = aggregation.maximumChildren
    ?.let { maximum ->
      fractionQualified.sortedByDescending(HeatmapNode::layoutValue).take(maximum)
    }
    ?.mapTo(mutableSetOf(), HeatmapNode::id)
    ?: fractionQualified.mapTo(mutableSetOf(), HeatmapNode::id)
  val retained = children.filter { it.id in retainedIds }
  val omitted = children.filterNot { it.id in retainedIds }
  if (omitted.isEmpty()) return children

  val othersValue = omitted.sumOf(HeatmapNode::layoutValue)
  val weightedMetric = omitted.mapNotNull { node ->
    node.effectiveMetric?.let { metric -> node.layoutValue to metric }
  }.let { measuredNodes ->
    val weight = measuredNodes.sumOf { it.first }
    if (weight > 0.0) measuredNodes.sumOf { (value, metric) -> value * metric } / weight else null
  }
  val baseId = "volcano::others"
  val otherId = generateSequence(baseId) { "$it-" }
    .first { candidate -> children.none { it.id == candidate } }

  val others = HeatmapNode(
    id = otherId,
    label = aggregation.othersLabel,
    value = othersValue,
    metric = weightedMetric,
  )
  onAggregated(others, omitted)
  return retained + others
}

/** Display data plus the original direct children represented by each generated Others cell. */
public data class HeatmapDisplayTree(
  val root: HeatmapNode,
  val aggregatedSources: Map<HeatmapPath, List<HeatmapNode>>,
)

/**
 * Like [toDisplayTree], retaining source nodes for a host-owned Others detail list.
 * Keys are root-relative paths, so identical IDs in different branches remain unambiguous.
 */
public fun HeatmapNode.toDisplayTreeWithSources(
  sort: HeatmapSort = HeatmapSort.LAYOUT_VALUE_DESCENDING,
  aggregation: HeatmapAggregation = HeatmapAggregation(),
): HeatmapDisplayTree {
  val sources = mutableMapOf<HeatmapPath, List<HeatmapNode>>()
  fun transform(node: HeatmapNode, path: List<String>): HeatmapNode {
    val children = node.children.map { transform(it, path + it.id) }
    val aggregated = aggregateChildren(children, aggregation) { others, omitted ->
      val omittedIds = omitted.mapTo(mutableSetOf(), HeatmapNode::id)
      sources[HeatmapPath(path + others.id)] = node.children.filter { it.id in omittedIds }
    }
    return node.copy(children = aggregated)
  }
  val displayRoot = transform(this, emptyList()).sorted(sort)
  class SourcePathNode {
    val children = mutableMapOf<String, SourcePathNode>()
    var sources: List<HeatmapNode>? = null
  }
  val sourcePaths = SourcePathNode()
  sources.forEach { (path, nodes) ->
    var current = sourcePaths
    path.nodeIds.forEach { id ->
      current = current.children.getOrPut(id, ::SourcePathNode)
    }
    current.sources = nodes
  }

  // Walk only path prefixes represented by source entries instead of resolving every path from
  // the root. This keeps wide trees with many aggregated groups linear in their display size.
  val visibleSources = mutableMapOf<HeatmapPath, List<HeatmapNode>>()
  val visiblePath = mutableListOf<String>()
  fun collectVisibleSources(node: HeatmapNode, sourcePath: SourcePathNode) {
    sourcePath.sources?.let { visibleSources[HeatmapPath(visiblePath)] = it }
    node.children.forEach { child ->
      val childSourcePath = sourcePath.children[child.id] ?: return@forEach
      visiblePath += child.id
      collectVisibleSources(child, childSourcePath)
      visiblePath.removeAt(visiblePath.lastIndex)
    }
  }
  collectVisibleSources(displayRoot, sourcePaths)
  return HeatmapDisplayTree(displayRoot, visibleSources)
}

/** Applies aggregation first, then a stable recursive ordering for presentation. */
public fun HeatmapNode.toDisplayTree(
  sort: HeatmapSort = HeatmapSort.LAYOUT_VALUE_DESCENDING,
  aggregation: HeatmapAggregation = HeatmapAggregation(),
): HeatmapNode = aggregateSmallChildren(aggregation).sorted(sort)
