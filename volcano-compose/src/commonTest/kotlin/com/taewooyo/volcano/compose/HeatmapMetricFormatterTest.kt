/*
 * Copyright (C) 2026 taewooyo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

class HeatmapMetricFormatterTest {

  @Test
  fun `formats signed and percentage metrics`() {
    assertEquals("+1.5", SignedMetricFormatter.format(1.5))
    assertEquals("-1.50%", PercentageMetricFormatter.format(-1.5))
    assertEquals("-6.55%", PercentageMetricFormatter.format(-6.550000000000001))
  }

  @Test
  fun `cell accessibility description includes a formatted metric when available`() {
    val node = HeatmapNode(id = "latency", label = "Latency", value = 1.0, metric = -2.5)

    assertEquals(
      "Latency, metric -2.50%",
      defaultHeatmapCellContentDescription(node, PercentageMetricFormatter),
    )
  }

  @Test
  fun `cell accessibility description omits a missing metric`() {
    val node = HeatmapNode(id = "latency", label = "Latency", value = 1.0)

    assertEquals("Latency", defaultHeatmapCellContentDescription(node, PercentageMetricFormatter))
  }
}
