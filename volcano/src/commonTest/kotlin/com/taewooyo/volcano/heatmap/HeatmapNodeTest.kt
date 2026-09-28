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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class HeatmapNodeTest {

  @Test
  fun `group layout value is derived from positive descendants`() {
    val node = HeatmapNode(
      id = "market",
      label = "Market",
      value = 100.0,
      children = listOf(
        HeatmapNode(id = "a", label = "A", value = 20.0),
        HeatmapNode(id = "b", label = "B", value = 30.0),
      ),
    )

    assertEquals(50.0, node.layoutValue)
  }

  @Test
  fun `resolving an obsolete route keeps the nearest visible group`() {
    val root = HeatmapNode(
      id = "market",
      label = "Market",
      value = 1.0,
      children = listOf(HeatmapNode(id = "technology", label = "Technology", value = 1.0)),
    )

    assertEquals("technology", root.resolve(HeatmapPath(listOf("technology", "missing"))).id)
  }

  @Test
  fun `duplicate sibling ids are rejected`() {
    assertFailsWith<IllegalArgumentException> {
      HeatmapNode(
        id = "market",
        label = "Market",
        value = 1.0,
        children = listOf(
          HeatmapNode(id = "same", label = "A", value = 1.0),
          HeatmapNode(id = "same", label = "B", value = 1.0),
        ),
      )
    }
  }

  @Test
  fun `group metric is derived from weighted descendants`() {
    val node = HeatmapNode(
      id = "sector",
      label = "Sector",
      value = 0.0,
      children = listOf(
        HeatmapNode(id = "a", label = "A", value = 3.0, metric = 10.0),
        HeatmapNode(id = "b", label = "B", value = 1.0, metric = -2.0),
      ),
    )

    assertEquals(7.0, node.effectiveMetric)
  }

  @Test
  fun `copy recomputes derived values without changing the previous snapshot`() {
    val leaf = HeatmapNode("leaf", "Leaf", 2.0, metric = 3.0)
    val root = HeatmapNode("root", "Root", 0.0, children = listOf(leaf))
    val updated = root.copy(children = listOf(leaf.copy(value = 7.0, metric = -4.0)))
    assertEquals(2.0, root.layoutValue)
    assertEquals(3.0, root.effectiveMetric)
    assertEquals(7.0, updated.layoutValue)
    assertEquals(-4.0, updated.effectiveMetric)
  }

  @Test
  fun `node owns a snapshot of mutable child lists`() {
    val first = HeatmapNode("first", "First", 2.0, metric = 3.0)
    val second = HeatmapNode("second", "Second", 5.0, metric = -1.0)
    val sourceChildren = mutableListOf(first)
    val root = HeatmapNode("root", "Root", 0.0, children = sourceChildren)

    sourceChildren += second

    assertEquals(listOf(first), root.children)
    assertEquals(2.0, root.layoutValue)
    assertEquals(3.0, root.effectiveMetric)
    assertEquals(root, root.copy())
    assertEquals(root.hashCode(), root.copy().hashCode())
    val (id, label, value, color, children, metric, imageUrl) = root
    assertEquals("root", id)
    assertEquals("Root", label)
    assertEquals(0.0, value)
    assertEquals(null, color)
    assertEquals(listOf(first), children)
    assertEquals(null, metric)
    assertEquals(null, imageUrl)
  }

  @Test
  fun `path owns a snapshot of mutable node ids`() {
    val sourceIds = mutableListOf("market", "technology")
    val path = HeatmapPath(sourceIds)

    sourceIds += "semiconductors"

    assertEquals(listOf("market", "technology"), path.nodeIds)
    assertEquals(path, path.copy())
    assertEquals(path.hashCode(), path.copy().hashCode())
    val (nodeIds) = path
    assertEquals(listOf("market", "technology"), nodeIds)
  }

  @Test
  fun `child snapshot cannot be changed through a mutable cast`() {
    val leaf = HeatmapNode("leaf", "Leaf", 1.0)
    val root = HeatmapNode("root", "Root", 0.0, children = listOf(leaf))

    assertFails {
      (root.children as MutableList<HeatmapNode>).add(HeatmapNode("next", "Next", 1.0))
    }
  }
}
