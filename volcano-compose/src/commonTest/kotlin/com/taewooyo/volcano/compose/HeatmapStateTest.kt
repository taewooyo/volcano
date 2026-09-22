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

import com.taewooyo.volcano.heatmap.HeatmapNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
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
    val state = HeatmapState(HeatmapNode(id = "market", label = "Market", value = 1.0))

    state.select(first)

    assertTrue(state.isSelected(first))
    assertFalse(state.isSelected(second))
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
}
