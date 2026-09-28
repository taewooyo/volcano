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

import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.HeatmapPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeatmapStateTest {

  @Test
  fun `drill down updates the visible group and navigate up restores its parent`() {
    val state = HeatmapState(
      HeatmapNode(
        id = "market",
        label = "Market",
        value = 1.0,
        children = listOf(
          HeatmapNode(
            id = "technology",
            label = "Technology",
            value = 1.0,
            children = listOf(HeatmapNode(id = "chip", label = "Chip", value = 1.0)),
          ),
        ),
      ),
    )

    assertFalse(state.canNavigateUp)
    assertTrue(state.drillDown("technology"))
    assertEquals("technology", state.visibleNode.id)
    assertTrue(state.canNavigateUp)
    assertEquals(listOf("market", "technology"), state.breadcrumbs.map(HeatmapNode::id))
    assertTrue(state.navigateUp())
    assertEquals("market", state.visibleNode.id)
    assertFalse(state.canNavigateUp)
    assertFalse(state.navigateUp())
  }

  @Test
  fun `breadcrumb depth navigation remains correct when ancestor ids repeat`() {
    val nested = HeatmapNode(
      id = "duplicate",
      label = "Nested",
      value = 1.0,
      children = listOf(HeatmapNode(id = "leaf", label = "Leaf", value = 1.0)),
    )
    val state = HeatmapState(
      HeatmapNode(
        id = "root",
        label = "Root",
        value = 1.0,
        children = listOf(
          HeatmapNode(
            id = "duplicate",
            label = "First",
            value = 1.0,
            children = listOf(nested),
          ),
        ),
      ),
    )

    assertTrue(state.drillDown("duplicate"))
    assertTrue(state.drillDown("duplicate"))
    assertEquals(listOf("Root", "First", "Nested"), state.breadcrumbs.map(HeatmapNode::label))

    assertTrue(state.navigateToBreadcrumb(1))
    assertEquals("First", state.visibleNode.label)
  }

  @Test
  fun `selecting a leaf does not change navigation`() {
    val leaf = HeatmapNode(id = "stock", label = "Stock", value = 1.0)
    val state = HeatmapState(HeatmapNode("market", "Market", 1.0, children = listOf(leaf)))

    state.select(leaf)

    assertEquals("stock", state.selectedId)
    assertEquals(leaf, state.selectedNode)
    assertTrue(state.isSelected(leaf))
    assertEquals("market", state.visibleNode.id)
    state.clearSelection()
    assertEquals(null, state.selectedId)
    assertEquals(null, state.selectedNode)
  }

  @Test
  fun `selection identity does not select a different leaf that has the same id`() {
    val first = HeatmapNode(id = "duplicate", label = "First", value = 1.0)
    val second = HeatmapNode(id = "duplicate", label = "Second", value = 1.0)
    val state = HeatmapState(
      HeatmapNode(
        id = "market",
        label = "Market",
        value = 0.0,
        children = listOf(
          HeatmapNode("first-group", "First group", 0.0, children = listOf(first)),
          HeatmapNode("second-group", "Second group", 0.0, children = listOf(second)),
        ),
      ),
    )

    state.select(first)

    assertTrue(state.isSelected(first))
    assertFalse(state.isSelected(second))
    assertEquals(HeatmapPath(listOf("first-group", "duplicate")), state.selectionPath)
  }

  @Test
  fun `select ignores leaves outside the state tree`() {
    val inside = HeatmapNode("inside", "Inside", 1.0)
    val outside = HeatmapNode("outside", "Outside", 1.0)
    val state = HeatmapState(HeatmapNode("root", "Root", 0.0, children = listOf(inside)))

    state.select(inside)
    state.select(outside)

    assertEquals(inside, state.selectedNode)
    assertEquals(HeatmapPath(listOf("inside")), state.selectionPath)
    assertFalse(state.isSelected(outside))
  }

  @Test
  fun `motion validates duration and initial scale`() {
    assertFailsWith<IllegalArgumentException> { HeatmapMotion(durationMillis = -1) }
    assertFailsWith<IllegalArgumentException> { HeatmapMotion(initialScale = 0f) }
    assertFailsWith<IllegalArgumentException> { HeatmapMotion(initialScale = 1.01f) }
    assertFailsWith<IllegalArgumentException> { HeatmapMotion(pressScale = 0f) }
    assertFailsWith<IllegalArgumentException> { HeatmapMotion(pressedAlpha = 1.01f) }
    assertFailsWith<IllegalArgumentException> { HeatmapMotion(pressDurationMillis = -1) }
  }

  @Test
  fun `interaction rejects a negative tooltip duration`() {
    assertFailsWith<IllegalArgumentException> { HeatmapInteraction(tooltipDurationMillis = -1) }
    assertFailsWith<IllegalArgumentException> { HeatmapInteraction(tooltipHoverDelayMillis = -1) }
  }

  @Test
  fun `refresh preserves nested navigation and resolves selection to the new node`() {
    val leaf = HeatmapNode("leaf", "Before", 1.0)
    val nested = HeatmapNode("group", "Nested", 0.0, children = listOf(leaf))
    val outer = HeatmapNode("group", "Outer", 0.0, children = listOf(nested))
    val root = HeatmapNode("root", "Root", 0.0, children = listOf(outer))
    val state = HeatmapState(root)
    assertTrue(state.drillDown(nested))
    state.select(leaf)
    val updatedLeaf = leaf.copy(label = "After", value = 5.0)
    val updatedNested = nested.copy(children = listOf(updatedLeaf))
    state.updateRoot(root.copy(children = listOf(outer.copy(children = listOf(updatedNested)))))
    assertEquals(HeatmapPath(listOf("group", "group")), state.navigationPath)
    assertTrue(state.visibleNode === updatedNested)
    assertTrue(state.selectedNode === updatedLeaf)
    assertEquals(HeatmapPath(listOf("group", "group", "leaf")), state.selectionPath)
    assertFalse(state.isSelected(leaf))
  }

  @Test
  fun `removal falls back to a surviving group and clears deleted selection`() {
    val leaf = HeatmapNode("leaf", "Leaf", 1.0)
    val nested = HeatmapNode("nested", "Nested", 0.0, children = listOf(leaf))
    val sibling = HeatmapNode("other", "Other", 1.0)
    val outer = HeatmapNode("outer", "Outer", 0.0, children = listOf(nested, sibling))
    val root = HeatmapNode("root", "Root", 0.0, children = listOf(outer))
    val state = HeatmapState(root)
    state.drillDown(nested)
    state.select(leaf)
    state.updateRoot(root.copy(children = listOf(outer.copy(children = listOf(sibling)))))
    assertEquals(HeatmapPath(listOf("outer")), state.navigationPath)
    assertEquals(null, state.selectedNode)
    assertTrue(state.navigateUp())
    assertFalse(state.canNavigateUp)
  }

  @Test
  fun `invalid and leaf paths leave navigation unchanged and a new root resets it`() {
    val leaf = HeatmapNode("leaf", "Leaf", 1.0)
    val group = HeatmapNode("group", "Group", 0.0, children = listOf(leaf))
    val root = HeatmapNode("root", "Root", 0.0, children = listOf(group))
    val state = HeatmapState(root)
    assertTrue(state.navigateToPath(HeatmapPath(listOf("group"))))
    assertFalse(state.navigateToPath(HeatmapPath(listOf("missing"))))
    assertFalse(state.navigateToPath(HeatmapPath(listOf("group", "leaf"))))
    assertEquals("group", state.visibleNode.id)
    state.select(leaf)
    state.updateRoot(root.copy(id = "another-root"))
    assertFalse(state.canNavigateUp)
    assertEquals(null, state.selectedNode)
  }
}
