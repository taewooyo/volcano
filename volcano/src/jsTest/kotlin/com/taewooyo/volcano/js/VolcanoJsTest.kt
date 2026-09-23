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
package com.taewooyo.volcano.js

import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.SignedMetricColorScale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VolcanoJsTest {

  @Test
  fun `returns four geometry values per input value`() {
    val geometry = measureTreemap(doubleArrayOf(6.0, 3.0, 1.0), width = 300, height = 200)

    assertEquals(12, geometry.size)
    geometry.toList().chunked(4).forEach { (x, y, width, height) ->
      assertTrue(x >= 0.0)
      assertTrue(y >= 0.0)
      assertTrue(width >= 0.0)
      assertTrue(height >= 0.0)
      assertTrue(x + width <= 300.0)
      assertTrue(y + height <= 200.0)
    }
  }

  @Test
  fun `lays out nested nodes and applies the shared color scale`() {
    val result = layoutHeatmap(
      ids = arrayOf("root", "group", "up", "down"),
      labels = arrayOf("Market", "Tech", "A", "B"),
      parentIndexes = intArrayOf(-1, 0, 1, 1),
      values = doubleArrayOf(0.0, 0.0, 6.0, 4.0),
      metrics = doubleArrayOf(Double.NaN, Double.NaN, 5.0, -5.0),
      explicitColors = doubleArrayOf(Double.NaN, Double.NaN, Double.NaN, 4_278_190_335.0),
      width = 300,
      height = 200,
      groupHeaderHeight = 18,
      maximumAbsoluteMetric = 10.0,
    )

    assertEquals(20, result.size)
    assertEquals(18.0, result[2 * 5 + 1])
    assertEquals(4_278_190_335.0, result[3 * 5 + 4])
    assertEquals(
      SignedMetricColorScale().colorOf(
        HeatmapNode(id = "group", label = "Tech", value = 10.0, metric = 1.0),
      ).toDouble(),
      result[1 * 5 + 4],
    )
  }

  @Test
  fun `retains core input rules and marks nodes without visual area`() {
    val result = layoutHeatmap(
      ids = arrayOf("root", "visible", "hidden"),
      labels = arrayOf("Market", "Visible", ""),
      parentIndexes = intArrayOf(-1, 0, 0),
      values = doubleArrayOf(0.0, 1.0, -1.0),
      metrics = doubleArrayOf(Double.NaN, 1.0, 2.0),
      explicitColors = doubleArrayOf(Double.NaN, Double.NaN, Double.NaN),
      width = 300,
      height = 200,
      groupHeaderHeight = 18,
      maximumAbsoluteMetric = 10.0,
    )

    assertTrue(result[2 * 5].isNaN())
    assertTrue(result[2 * 5 + 1].isNaN())
    assertEquals(0.0, result[2 * 5 + 2])
    assertEquals(0.0, result[2 * 5 + 3])
    assertEquals(
      SignedMetricColorScale().colorOf(
        HeatmapNode(id = "hidden", label = "", value = -1.0, metric = 2.0),
      ).toDouble(),
      result[2 * 5 + 4],
    )
  }

  @Test
  fun `accepts a custom palette without changing the default entry point`() {
    val ids = arrayOf("root", "up", "down")
    val labels = arrayOf("Market", "Up", "Down")
    val parents = intArrayOf(-1, 0, 0)
    val values = doubleArrayOf(0.0, 6.0, 4.0)
    val metrics = doubleArrayOf(Double.NaN, 10.0, -10.0)
    val colors = doubleArrayOf(Double.NaN, Double.NaN, Double.NaN)
    val custom = layoutHeatmapWithPalette(
      ids, labels, parents, values, metrics, colors,
      width = 300, height = 200, groupHeaderHeight = 0, maximumAbsoluteMetric = 10.0,
      negativeColor = 0xFFE53935L.toDouble(),
      neutralColor = 0xFF9CA3AFL.toDouble(),
      positiveColor = 0xFF16A34AL.toDouble(),
    )
    val defaults = layoutHeatmap(
      ids, labels, parents, values, metrics, colors,
      width = 300, height = 200, groupHeaderHeight = 0, maximumAbsoluteMetric = 10.0,
    )

    assertEquals(0xFF16A34AL.toDouble(), custom[1 * 5 + 4])
    assertEquals(0xFFE53935L.toDouble(), custom[2 * 5 + 4])
    assertEquals(SignedMetricColorScale().positive.toDouble(), defaults[1 * 5 + 4])
    assertEquals(defaults[1], custom[1])
  }
}
