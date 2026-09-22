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
package com.taewooyo.volcano.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.HeatmapPath
import com.taewooyo.volcano.heatmap.resolve

/** State holder for drill-down navigation in [Heatmap]. */
@Stable
public class HeatmapState internal constructor(
  private val root: HeatmapNode,
) {

  private var path: HeatmapPath by mutableStateOf(HeatmapPath())
  private var selectedLeaf: HeatmapNode? by mutableStateOf(null)

  public val visibleNode: HeatmapNode
    get() = root.resolve(path)

  /** The root-to-visible path, including the visible group. */
  public val breadcrumbs: List<HeatmapNode>
    get() {
      val nodes = mutableListOf(root)
      var current = root
      path.nodeIds.forEach { id ->
        val child = current.children.firstOrNull { it.id == id } ?: return nodes
        nodes += child
        current = child
      }
      return nodes
    }

  /** The leaf selected by the consumer, including its domain data, if any. */
  public val selectedNode: HeatmapNode?
    get() = selectedLeaf

  /** The id of [selectedNode], retained as a convenient lightweight selection value. */
  public val selectedId: String?
    get() = selectedLeaf?.id

  /** Whether a platform back action can return from the current group to its parent. */
  public val canNavigateUp: Boolean
    get() = path.nodeIds.isNotEmpty()

  /** Opens a direct child group of the currently visible node. */
  public fun drillDown(nodeId: String): Boolean {
    val target = visibleNode.children.firstOrNull { it.id == nodeId } ?: return false
    if (target.isLeaf) return false
    path = HeatmapPath(path.nodeIds + nodeId)
    return true
  }

  /** Navigates one level upward, returning whether the visible group changed. */
  public fun navigateUp(): Boolean {
    if (path.nodeIds.isEmpty()) return false
    path = HeatmapPath(path.nodeIds.dropLast(1))
    return true
  }

  /** Navigates to an item in [breadcrumbs]. */
  public fun navigateTo(nodeId: String): Boolean {
    val index = breadcrumbs.indexOfFirst { it.id == nodeId }
    if (index < 0) return false
    return navigateToBreadcrumb(index)
  }

  /**
   * Navigates to an item at [index] in [breadcrumbs].
   *
   * Prefer this over [navigateTo] for breadcrumb UI because node ids are only required to be
   * unique among siblings, not across an entire hierarchy.
   */
  public fun navigateToBreadcrumb(index: Int): Boolean {
    if (index !in breadcrumbs.indices || index == breadcrumbs.lastIndex) return false
    path = HeatmapPath(path.nodeIds.take(index))
    return true
  }

  /** Returns to the initial heatmap root. */
  public fun reset() {
    path = HeatmapPath()
  }

  /** Marks a leaf as selected. Selection does not change the visible navigation path. */
  public fun select(node: HeatmapNode) {
    if (node.isLeaf) selectedLeaf = node
  }

  /** Whether [node] is the exact leaf currently selected in this heatmap tree. */
  public fun isSelected(node: HeatmapNode): Boolean = selectedLeaf === node

  /** Clears the current leaf selection. */
  public fun clearSelection() {
    selectedLeaf = null
  }
}

/** Creates and remembers a drill-down state for [root]. */
@Composable
public fun rememberHeatmapState(root: HeatmapNode): HeatmapState = remember(root) {
  HeatmapState(root)
}
