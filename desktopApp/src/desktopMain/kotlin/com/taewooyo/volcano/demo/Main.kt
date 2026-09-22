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
package com.taewooyo.volcano.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.taewooyo.volcano.compose.Heatmap
import com.taewooyo.volcano.compose.HeatmapBackButton
import com.taewooyo.volcano.compose.HeatmapBreadcrumb
import com.taewooyo.volcano.compose.HeatmapLegend
import com.taewooyo.volcano.compose.HeatmapDisplayPolicy
import com.taewooyo.volcano.compose.HeatmapInteraction
import com.taewooyo.volcano.compose.PercentageMetricFormatter
import com.taewooyo.volcano.compose.rememberHeatmapState
import com.taewooyo.volcano.compose.coil.CoilHeatmapLogo
import com.taewooyo.volcano.heatmap.HeatmapAggregation
import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.SignedMetricColorScale
import com.taewooyo.volcano.heatmap.legendEntries
import com.taewooyo.volcano.heatmap.toDisplayTree

/** Compose Desktop demo mirroring the Android market heatmap experience. */
public fun main() = application {
  Window(
    title = "Volcano Market Heatmap",
    onCloseRequest = ::exitApplication,
  ) {
    val colorScale = remember {
      SignedMetricColorScale(
        maximumAbsoluteMetric = 10.0,
        negative = 0xFFE53935,
        neutral = 0xFF9CA3AF,
        positive = 0xFF16A34A,
      )
    }
      val root = remember { marketMap.toDisplayTree(aggregation = HeatmapAggregation(maximumChildren = 24)) }
      val state = rememberHeatmapState(root)

      Column(
        Modifier
          .fillMaxSize()
          .safeDrawingPadding()
          .background(Color(0xFFF7F8FA))
          .padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          HeatmapBackButton(onClick = state::navigateUp, enabled = state.canNavigateUp)
          HeatmapLegend(
            entries = colorScale.legendEntries().map { it.copy(label = "${it.label}%") },
            contentColor = Color(0xFF475569),
          )
        }
        HeatmapBreadcrumb(state = state, modifier = Modifier.fillMaxWidth())
        Box(Modifier.fillMaxSize().padding(top = 8.dp)) {
          Heatmap(
            state = state,
            modifier = Modifier.fillMaxSize(),
            colorScale = colorScale,
            interaction = HeatmapInteraction(showTooltipOnHover = true),
            displayPolicy = HeatmapDisplayPolicy(metricFormatter = PercentageMetricFormatter),
            logoContent = { node, size -> CoilHeatmapLogo(node = node, size = size) },
            onLeafClick = { node -> println("Selected ${node.id}: ${node.label}") },
          )
        }
      }
  }
}

private val marketMap = HeatmapNode(
  id = "market",
  label = "Market",
  value = 1.0,
  children = listOf(
    HeatmapNode(
      id = "technology",
      label = "Electronic Technology",
      value = 1.0,
      children = listOf(
        HeatmapNode(id = "samsung", label = "Samsung", value = 48_000.0, metric = 2.3, imageUrl = logoUrl("Samsung")),
        HeatmapNode(id = "sk-hynix", label = "SK Hynix", value = 31_000.0, metric = 1.4, imageUrl = logoUrl("SKHynix")),
        HeatmapNode(id = "hanmi", label = "Hanmi Semi", value = 8_000.0, metric = -2.4, imageUrl = logoUrl("Hanmi")),
      ),
    ),
    HeatmapNode(
      id = "finance",
      label = "Finance",
      value = 1.0,
      children = listOf(
        HeatmapNode(id = "kb", label = "KB Financial", value = 44_000.0, metric = 2.6, imageUrl = logoUrl("KB")),
        HeatmapNode(id = "shinhan", label = "Shinhan", value = 36_000.0, metric = 1.2, imageUrl = logoUrl("Shinhan")),
        HeatmapNode(id = "hana", label = "Hana", value = 12_000.0, metric = -0.8, imageUrl = logoUrl("Hana")),
      ),
    ),
    HeatmapNode(
      id = "healthcare",
      label = "Health Technology",
      value = 1.0,
      children = listOf(
        HeatmapNode(id = "samsung-bio", label = "Samsung Biologics", value = 26_000.0, metric = 0.2, imageUrl = logoUrl("SamsungBio")),
        HeatmapNode(id = "celltrion", label = "Celltrion", value = 19_000.0, metric = -1.1, imageUrl = logoUrl("Celltrion")),
        HeatmapNode(id = "yuhan", label = "Yuhan", value = 15_000.0, metric = 1.6, imageUrl = logoUrl("Yuhan")),
      ),
    ),
  ),
)

private fun logoUrl(label: String): String =
  "https://ui-avatars.com/api/?name=$label&background=0F172A&color=FFFFFF&bold=true&size=128"
