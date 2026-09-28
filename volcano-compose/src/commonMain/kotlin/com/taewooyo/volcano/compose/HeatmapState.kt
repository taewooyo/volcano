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
package com.taewooyo.volcano.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.HeatmapPath
import com.taewooyo.volcano.heatmap.resolve

/** State holder for drill-down navigation in [Heatmap]. */
@Stable
public class HeatmapState internal constructor(root: HeatmapNode) {

  private var root: HeatmapNode by mutableStateOf(root, referentialEqualityPolicy())
  private var path: HeatmapPath by mutableStateOf(HeatmapPath())
  private var selectedLeaf: HeatmapNode? by mutableStateOf(null, referentialEqualityPolicy())
  private var selectedPath: HeatmapPath? by mutableStateOf(null)

  /** Root-relative group path. IDs may repeat in different branches. */
  public val navigationPath: HeatmapPath
    get() = path

  /** Root-relative selected leaf path, suitable for the display tree aggregatedSources mapping. */
  public val selectionPath: HeatmapPath?
    get() = selectedPath

  /** Replaces immutable data while retaining valid navigation and selection paths. */
  public fun updateRoot(root: HeatmapNode) {
    if (this.root === root || this.root == root) return
    if (this.root.id != root.id) {
      this.root = root
      reset()
      clearSelection()
      return
    }
    var current = root
    val validIds = mutableListOf<String>()
    for (id in path.nodeIds) {
      val child = current.children.firstOrNull { it.id == id && !it.isLeaf } ?: break
      validIds += id
      current = child
    }
    this.root = root
    path = HeatmapPath(validIds)
    selectedLeaf = selectedPath?.let { resolveExact(it) }?.takeIf { it.isLeaf }
    if (selectedLeaf == null) selectedPath = null
  }

  /** Opens any group by its complete root-relative path. Invalid paths leave state unchanged. */
  public fun navigateToPath(path: HeatmapPath): Boolean {
    val target = resolveExact(path) ?: return false
    if (target.isLeaf && path.nodeIds.isNotEmpty()) return false
    if (this.path == path) return false
    this.path = HeatmapPath(path.nodeIds.toList())
    return true
  }

  /** Opens an exact group from the current tree, including deeply nested visible groups. */
  public fun drillDown(node: HeatmapNode): Boolean {
    if (node.isLeaf) return false
    return findPath(root, node)?.let(::navigateToPath) ?: false
  }

  internal fun pathOf(node: HeatmapNode): HeatmapPath? = findPath(root, node)

  internal fun nodeAtPath(path: HeatmapPath): HeatmapNode? = resolveExact(path)

  private fun resolveExact(path: HeatmapPath): HeatmapNode? {
    var current = root
    for (id in path.nodeIds) {
      current = current.children.firstOrNull { it.id == id } ?: return null
    }
    return current
  }

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
    val path = findPath(root, node) ?: return
    if (node.isLeaf) {
      selectedPath = path
      selectedLeaf = node
    }
  }

  /** Whether [node] is the exact leaf currently selected in this heatmap tree. */
  public fun isSelected(node: HeatmapNode): Boolean = selectedLeaf === node

  /** Clears the current leaf selection. */
  public fun clearSelection() {
    selectedLeaf = null
    selectedPath = null
  }
}

/** Creates and remembers a drill-down state for [root]. */
@Composable
public fun rememberHeatmapState(root: HeatmapNode): HeatmapState {
  val state = remember { HeatmapState(root) }
  SideEffect { state.updateRoot(root) }
  return state
}

private fun findPath(root: HeatmapNode, target: HeatmapNode): HeatmapPath? {
  if (root === target) return HeatmapPath()
  data class Frame(val node: HeatmapNode, var nextChild: Int, val depth: Int)
  val stack = mutableListOf(Frame(root, nextChild = 0, depth = 0))
  val path = mutableListOf<String>()
  while (stack.isNotEmpty()) {
    val frame = stack.last()
    if (frame.nextChild >= frame.node.children.size) {
      stack.removeAt(stack.lastIndex)
      val parentDepth = (frame.depth - 1).coerceAtLeast(0)
      while (path.size > parentDepth) path.removeAt(path.lastIndex)
      continue
    }
    val child = frame.node.children[frame.nextChild++]
    while (path.size > frame.depth) path.removeAt(path.lastIndex)
    path += child.id
    if (child === target) return HeatmapPath(path)
    if (child.children.isNotEmpty()) {
      stack += Frame(child, nextChild = 0, depth = path.size)
    } else {
      path.removeAt(path.lastIndex)
    }
  }
  return null
}
