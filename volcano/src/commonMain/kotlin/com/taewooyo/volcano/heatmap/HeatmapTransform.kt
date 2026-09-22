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
public fun HeatmapNode.aggregateSmallChildren(
  aggregation: HeatmapAggregation,
): HeatmapNode = copy(
  children = aggregateChildren(children.map { it.aggregateSmallChildren(aggregation) }, aggregation),
)

private fun aggregateChildren(
  children: List<HeatmapNode>,
  aggregation: HeatmapAggregation,
): List<HeatmapNode> {
  if (children.isEmpty()) return children
  val totalValue = children.sumOf(HeatmapNode::layoutValue)
  if (totalValue <= 0.0) return children

  val fractionQualified = children.filter { child ->
    child.layoutValue / totalValue >= aggregation.minimumChildFraction
  }
  val retainedIds = aggregation.maximumChildren
    ?.let { maximum -> fractionQualified.sortedByDescending(HeatmapNode::layoutValue).take(maximum) }
    ?.mapTo(mutableSetOf(), HeatmapNode::id)
    ?: fractionQualified.mapTo(mutableSetOf(), HeatmapNode::id)
  val retained = children.filter { it.id in retainedIds }
  val omitted = children.filterNot { child -> child in retained }
  if (omitted.isEmpty()) return children

  val othersValue = omitted.sumOf(HeatmapNode::layoutValue)
  val weightedMetric = omitted.mapNotNull { node ->
    node.effectiveMetric?.let { metric -> node.layoutValue to metric }
  }.let { measuredNodes ->
    val weight = measuredNodes.sumOf { it.first }
    if (weight > 0.0) measuredNodes.sumOf { (value, metric) -> value * metric } / weight else null
  }
  val baseId = "${children.first().id.substringBefore("::")}::others"
  val otherId = generateSequence(baseId) { "$it-" }
    .first { candidate -> children.none { it.id == candidate } }

  return retained + HeatmapNode(
    id = otherId,
    label = aggregation.othersLabel,
    value = othersValue,
    metric = weightedMetric,
  )
}

/** Applies aggregation first, then a stable recursive ordering for presentation. */
public fun HeatmapNode.toDisplayTree(
  sort: HeatmapSort = HeatmapSort.LAYOUT_VALUE_DESCENDING,
  aggregation: HeatmapAggregation = HeatmapAggregation(),
): HeatmapNode = aggregateSmallChildren(aggregation).sorted(sort)
