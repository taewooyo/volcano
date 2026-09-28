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

/**
 * An immutable node rendered by a hierarchical heatmap.
 *
 * The constructor takes a snapshot of the supplied children. Derived weights and metrics are
 * calculated once from that snapshot; replace affected nodes with [copy] when source data changes.
 *
 * [value] controls the area assigned to a leaf. For a group, its descendants' values control the
 * layout whenever at least one descendant has a positive value. This permits a server to supply a
 * group total for display while keeping the visual area consistent with the visible children.
 *
 * [metric] is a domain-neutral signed indicator used by a [HeatmapColorScale]. [imageUrl] is an
 * optional consumer-owned image address; the core module never fetches it.
 */
public class HeatmapNode(
  public val id: String,
  public val label: String,
  public val value: Double,
  public val color: Long? = null,
  children: List<HeatmapNode> = emptyList(),
  public val metric: Double? = null,
  /** Optional image URL. Null and blank values mean that no image is requested. */
  public val imageUrl: String? = null,
) {

  /** A read-only snapshot, so cached derived values cannot diverge from the child tree. */
  public val children: List<HeatmapNode> = children.toSnapshotList()

  init {
    require(id.isNotBlank()) { "HeatmapNode.id must not be blank." }
    require(value.isFinite()) { "HeatmapNode.value must be finite." }
    require(metric == null || metric.isFinite()) { "HeatmapNode.metric must be finite when set." }
    val siblingIds = mutableSetOf<String>()
    require(this.children.all { siblingIds.add(it.id) }) {
      "Sibling HeatmapNode ids must be unique."
    }
  }

  public val isLeaf: Boolean
    get() = children.isEmpty()

  /** The positive value used by the layout engine. Invalid visual weights occupy no area. */
  public val layoutValue: Double = children.sumOf(HeatmapNode::layoutValue).let { childrenValue ->
    if (childrenValue > 0.0) childrenValue else value.coerceAtLeast(0.0)
  }

  /**
   * The node's metric, or a layout-value weighted metric derived from its descendants.
   *
   * This gives group headers a meaningful color when a data source provides metrics for leaves.
   */
  public val effectiveMetric: Double? = metric ?: run {
    var totalWeight = 0.0
    var weightedMetric = 0.0
    children.forEach { child ->
      child.effectiveMetric?.let { childMetric ->
        totalWeight += child.layoutValue
        weightedMetric += child.layoutValue * childMetric
      }
    }
    if (totalWeight > 0.0) weightedMetric / totalWeight else null
  }

  public operator fun component1(): String = id

  public operator fun component2(): String = label

  public operator fun component3(): Double = value

  public operator fun component4(): Long? = color

  public operator fun component5(): List<HeatmapNode> = children

  public operator fun component6(): Double? = metric

  public operator fun component7(): String? = imageUrl

  /** Returns a new node and takes a snapshot of [children]. */
  public fun copy(
    id: String = this.id,
    label: String = this.label,
    value: Double = this.value,
    color: Long? = this.color,
    children: List<HeatmapNode> = this.children,
    metric: Double? = this.metric,
    imageUrl: String? = this.imageUrl,
  ): HeatmapNode = HeatmapNode(id, label, value, color, children, metric, imageUrl)

  override fun equals(other: Any?): Boolean = this === other || (
    other is HeatmapNode &&
      id == other.id &&
      label == other.label &&
      value.toBits() == other.value.toBits() &&
      color == other.color &&
      children == other.children &&
      metric?.toBits() == other.metric?.toBits() &&
      imageUrl == other.imageUrl
  )

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + label.hashCode()
    result = 31 * result + value.hashCode()
    result = 31 * result + (color?.hashCode() ?: 0)
    result = 31 * result + children.hashCode()
    result = 31 * result + (metric?.hashCode() ?: 0)
    result = 31 * result + (imageUrl?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String =
    "HeatmapNode(id=$id, label=$label, value=$value, color=$color, children=$children, metric=$metric, imageUrl=$imageUrl)"
}

/** An immutable route from the heatmap root to a visible group. */
public class HeatmapPath(nodeIds: List<String> = emptyList()) {

  public val nodeIds: List<String> = nodeIds.toSnapshotList()

  init {
    require(this.nodeIds.none(String::isBlank)) { "HeatmapPath cannot contain a blank id." }
  }

  public operator fun component1(): List<String> = nodeIds

  public fun copy(nodeIds: List<String> = this.nodeIds): HeatmapPath = HeatmapPath(nodeIds)

  override fun equals(other: Any?): Boolean = this === other ||
    (other is HeatmapPath && nodeIds == other.nodeIds)

  override fun hashCode(): Int = nodeIds.hashCode()

  override fun toString(): String = "HeatmapPath(nodeIds=$nodeIds)"
}

/** Returns the node addressed by [path], or the closest valid ancestor when the data changed. */
public fun HeatmapNode.resolve(path: HeatmapPath): HeatmapNode {
  var current = this
  path.nodeIds.forEach { id ->
    current = current.children.firstOrNull { it.id == id } ?: return current
  }
  return current
}

private fun <T> List<T>.toSnapshotList(): List<T> {
  if (this is SnapshotList<*>) {
    @Suppress("UNCHECKED_CAST")
    return this as List<T>
  }
  return if (isEmpty()) emptyList() else SnapshotList(this)
}

private class SnapshotList<T>(source: List<T>) : AbstractList<T>() {
  private val values: List<T> = source.toList()

  override val size: Int
    get() = values.size

  override fun get(index: Int): T = values[index]
}
