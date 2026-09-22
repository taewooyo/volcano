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

/**
 * A node rendered by a hierarchical heatmap.
 *
 * [value] controls the area assigned to a leaf. For a group, its descendants' values control the
 * layout whenever at least one descendant has a positive value. This permits a server to supply a
 * group total for display while keeping the visual area consistent with the visible children.
 *
 * [metric] is a domain-neutral signed indicator used by a [HeatmapColorScale]. [imageUrl] is an
 * optional consumer-owned image address; the core module never fetches it.
 */
public data class HeatmapNode(
  val id: String,
  val label: String,
  val value: Double,
  val color: Long? = null,
  val children: List<HeatmapNode> = emptyList(),
  val metric: Double? = null,
  /** Optional image URL. Null and blank values mean that no image is requested. */
  val imageUrl: String? = null,
) {

  init {
    require(id.isNotBlank()) { "HeatmapNode.id must not be blank." }
    require(value.isFinite()) { "HeatmapNode.value must be finite." }
    require(metric == null || metric.isFinite()) { "HeatmapNode.metric must be finite when set." }
    require(children.map(HeatmapNode::id).distinct().size == children.size) {
      "Sibling HeatmapNode ids must be unique." 
    }
  }

  public val isLeaf: Boolean
    get() = children.isEmpty()

  /** The positive value used by the layout engine. Invalid visual weights occupy no area. */
  public val layoutValue: Double
    get() {
      val childrenValue = children.sumOf(HeatmapNode::layoutValue)
      return if (childrenValue > 0.0) childrenValue else value.coerceAtLeast(0.0)
    }

  /**
   * The node's metric, or a layout-value weighted metric derived from its descendants.
   *
   * This gives group headers a meaningful color when a data source provides metrics for leaves.
   */
  public val effectiveMetric: Double?
    get() {
      metric?.let { return it }
      val measuredChildren = children.mapNotNull { child ->
        child.effectiveMetric?.let { childMetric -> child to childMetric }
      }
      val totalWeight = measuredChildren.sumOf { (child, _) -> child.layoutValue }
      return if (totalWeight > 0.0) {
        measuredChildren.sumOf { (child, childMetric) -> child.layoutValue * childMetric } / totalWeight
      } else {
        null
      }
    }

}

/** An immutable route from the heatmap root to a visible group. */
public data class HeatmapPath(public val nodeIds: List<String> = emptyList()) {

  init {
    require(nodeIds.none(String::isBlank)) { "HeatmapPath cannot contain a blank id." }
  }
}

/** Returns the node addressed by [path], or the closest valid ancestor when the data changed. */
public fun HeatmapNode.resolve(path: HeatmapPath): HeatmapNode {
  var current = this
  path.nodeIds.forEach { id ->
    current = current.children.firstOrNull { it.id == id } ?: return current
  }
  return current
}
