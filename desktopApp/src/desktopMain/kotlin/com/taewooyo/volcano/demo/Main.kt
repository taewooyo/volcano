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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlin.math.sin
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
import kotlin.time.Duration.Companion.milliseconds

/** Compose Desktop demo mirroring the Android market heatmap experience. */
public fun main() = application {
  Window(
    title = "Volcano Market Heatmap",
    onCloseRequest = ::exitApplication,
  ) {
    var fastFeed by remember { mutableStateOf(false) }
    var dataMode by remember { mutableStateOf(DemoDataMode.Normal) }
    var demoTick by remember { mutableIntStateOf(0) }
    val changingMarketMap = remember(demoTick) { marketMap.withDemoMetrics(demoTick) }
    val expandedMarketMap = remember(changingMarketMap, dataMode) {
      if (dataMode == DemoDataMode.Normal) null else changingMarketMap.expandForHeatmapStressTest()
    }
    val displayRoot = remember(changingMarketMap, expandedMarketMap, dataMode) {
      when (dataMode) {
        DemoDataMode.Normal -> changingMarketMap
        DemoDataMode.Overview5K -> requireNotNull(expandedMarketMap).toDisplayTree(
          aggregation = HeatmapAggregation(maximumChildren = 12, othersLabel = "Others"),
        )
        DemoDataMode.Raw5K -> requireNotNull(expandedMarketMap)
      }
    }
    val colorScale = remember {
      SignedMetricColorScale(
        maximumAbsoluteMetric = 10.0,
        negative = 0xFFE53935,
        neutral = 0xFF9CA3AF,
        positive = 0xFF16A34A,
      )
    }
      val state = rememberHeatmapState(displayRoot)

      LaunchedEffect(demoTick, fastFeed, dataMode, state.canNavigateUp) {
        if (state.canNavigateUp) return@LaunchedEffect
        delay((if (fastFeed) 100L else 2_500L).milliseconds)
        demoTick += 1
      }

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
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            entries = colorScale.legendEntries().map { it.copy(label = "${it.label}%") },
            contentColor = Color(0xFF475569),
          )
          HeatmapBackButton(
            onClick = { dataMode = dataMode.next() },
            enabled = true,
            label = dataMode.nextActionLabel,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          androidx.compose.material.Text(
            text = "LIVE DEMO · sample metrics update every ${if (fastFeed) "100ms" else "2.5s"}",
            color = Color(0xFF64748B),
          )
          HeatmapBackButton(
            onClick = { fastFeed = !fastFeed },
            enabled = true,
            label = if (fastFeed) "Slow feed" else "Fast feed",
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

private enum class DemoDataMode(val nextActionLabel: String) {
  Normal("5K view"),
  Overview5K("5K raw"),
  Raw5K("Normal"),
  ;

  fun next(): DemoDataMode = when (this) {
    Normal -> Overview5K
    Overview5K -> Raw5K
    Raw5K -> Normal
  }
}

private fun HeatmapNode.expandForHeatmapStressTest(): HeatmapNode {
  val totalLeaves = children.sumOf { it.children.size }
  if (totalLeaves == 0) return this
  var assignedLeaves = 0
  val expandedChildren = children.mapIndexed { sectorIndex, sector ->
    val count = if (sectorIndex == children.lastIndex) {
      5_000 - assignedLeaves
    } else {
      (5_000.0 * sector.children.size / totalLeaves).toInt().also { assignedLeaves += it }
    }
    sector.copy(
      children = List(count) { index ->
        val source = sector.children[index % sector.children.size]
        source.copy(
          id = "${source.id}-${sector.id}-$index",
          label = "${source.label}-${index + 1}",
        )
      },
    )
  }
  return copy(children = expandedChildren)
}

private fun HeatmapNode.withDemoMetrics(tick: Int): HeatmapNode {
  var index = 0
  fun update(node: HeatmapNode): HeatmapNode {
    val updatedChildren = node.children.map(::update)
    val updatedMetric = node.metric?.let { metric ->
      metric + sin((tick * 0.8) + index++ * 1.7) * 5.0
    }
    return node.copy(children = updatedChildren, metric = updatedMetric)
  }
  return update(this)
}
