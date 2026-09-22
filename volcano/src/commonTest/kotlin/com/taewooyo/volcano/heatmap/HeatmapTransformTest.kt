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
import kotlin.test.assertNotNull

class HeatmapTransformTest {

  @Test
  fun `aggregates small children into others and keeps their weighted change`() {
    val market = HeatmapNode(
      id = "market",
      label = "Market",
      value = 100.0,
      children = listOf(
        HeatmapNode(id = "large", label = "Large", value = 80.0, metric = 2.0),
        HeatmapNode(id = "small-a", label = "Small A", value = 15.0, metric = -2.0),
        HeatmapNode(id = "small-b", label = "Small B", value = 5.0, metric = 4.0),
      ),
    )

    val display = market.toDisplayTree(
      aggregation = HeatmapAggregation(minimumChildFraction = 0.2),
    )

    assertEquals(listOf("Large", "Others"), display.children.map(HeatmapNode::label))
    val others = assertNotNull(display.children.firstOrNull { it.label == "Others" })
    assertEquals(20.0, others.value)
    assertEquals(-0.5, others.metric)
  }

  @Test
  fun `filters leaves and prunes groups with no matching instruments`() {
    val market = HeatmapNode(
      id = "market",
      label = "Market",
      value = 2.0,
      children = listOf(
        HeatmapNode(
          id = "technology",
          label = "Technology",
          value = 1.0,
          children = listOf(HeatmapNode(id = "keep", label = "Keep", value = 1.0)),
        ),
        HeatmapNode(
          id = "finance",
          label = "Finance",
          value = 1.0,
          children = listOf(HeatmapNode(id = "remove", label = "Remove", value = 1.0)),
        ),
      ),
    )

    val filtered = assertNotNull(market.filterLeaves { it.id == "keep" })

    assertEquals(listOf("technology"), filtered.children.map(HeatmapNode::id))
    assertEquals("keep", filtered.children.single().children.single().id)
  }
}
