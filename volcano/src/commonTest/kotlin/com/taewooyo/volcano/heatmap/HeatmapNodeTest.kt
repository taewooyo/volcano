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

import kotlin.test.Test
import kotlin.test.assertEquals
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
}
