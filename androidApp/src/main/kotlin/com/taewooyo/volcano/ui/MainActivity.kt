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
package com.taewooyo.volcano.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.taewooyo.volcano.compose.Heatmap
import com.taewooyo.volcano.compose.HeatmapBreadcrumb
import com.taewooyo.volcano.compose.HeatmapDisplayPolicy
import com.taewooyo.volcano.compose.HeatmapLegend
import com.taewooyo.volcano.compose.HeatmapBackButton
import com.taewooyo.volcano.compose.HeatmapInteraction
import com.taewooyo.volcano.compose.PercentageMetricFormatter
import com.taewooyo.volcano.compose.rememberHeatmapState
import com.taewooyo.volcano.compose.coil.CoilHeatmapLogo
import com.taewooyo.volcano.core.model.StockItem
import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.HeatmapAggregation
import com.taewooyo.volcano.heatmap.SignedMetricColorScale
import com.taewooyo.volcano.heatmap.legendEntries
import com.taewooyo.volcano.heatmap.toDisplayTree
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val context = LocalContext.current
      val scope = rememberCoroutineScope()
      val stocks = viewModel.stocks.collectAsState().value
      var dataMode by remember { mutableStateOf(DemoDataMode.Normal) }
      var fastFeed by remember { mutableStateOf(false) }
      var demoTick by remember { mutableIntStateOf(0) }
      val displayStocks = remember(stocks.stocks, dataMode, demoTick) {
        val changingStocks = stocks.stocks.mapIndexed { index, stock ->
          val baselinePercent = if (stock.value == 0.0) 0.0 else stock.oldValue / stock.value * 100.0
          val changingPercent = baselinePercent + sin((demoTick * 0.8) + index * 1.7) * 5.0
          stock.copy(oldValue = stock.value * changingPercent / 100.0)
        }
        if (dataMode == DemoDataMode.Normal) changingStocks else changingStocks.expandForHeatmapStressTest()
      }
      val sector = displayStocks.groupBy { it.type }
      val colorScale = remember {
        SignedMetricColorScale(
          maximumAbsoluteMetric = 10.0,
          negative = 0xFFE53935,
          neutral = 0xFF9CA3AF,
          positive = 0xFF16A34A,
        )
      }
      val sourceHeatmapRoot = remember(displayStocks) {
        HeatmapNode(
          id = "market",
          label = "Market",
          value = displayStocks.sumOf { it.value },
          children = sector.map { (type, items) ->
            HeatmapNode(
              id = "sector-$type",
              label = type,
              value = items.sumOf { it.value },
              children = items.map { stock ->
                val metric = if (stock.value == 0.0) {
                  0.0
                } else {
                  (stock.oldValue / stock.value) * 100
                }
                HeatmapNode(
                  id = "stock-${stock.name}",
                  label = stock.name,
                  value = stock.value,
                  metric = metric,
                  imageUrl = stock.logoUrl,
                )
              },
            )
          },
        )
      }
      val heatmapRoot = remember(sourceHeatmapRoot, dataMode) {
        if (dataMode == DemoDataMode.Overview5K) {
          sourceHeatmapRoot.toDisplayTree(
            aggregation = HeatmapAggregation(
              maximumChildren = 12,
              othersLabel = "Others",
            ),
          )
        } else {
          sourceHeatmapRoot
        }
      }
      val heatmapState = rememberHeatmapState(heatmapRoot)

      LaunchedEffect(dataMode, heatmapState.canNavigateUp, demoTick, fastFeed) {
        if (heatmapState.canNavigateUp) return@LaunchedEffect
        delay(if (fastFeed) 100.milliseconds else 2_500.milliseconds)
        demoTick += 1
      }

      BackHandler(enabled = heatmapState.canNavigateUp) {
        heatmapState.navigateUp()
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .safeDrawingPadding()
          .background(Color(0xFFF7F8FA))
          .padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        androidx.compose.foundation.layout.Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          HeatmapBackButton(
            onClick = heatmapState::navigateUp,
            enabled = heatmapState.canNavigateUp,
          )
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
        androidx.compose.foundation.layout.Row(
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
          horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "LIVE DEMO · sample metrics update every ${if (fastFeed) "100ms" else "2.5s"}",
            color = Color(0xFF64748B),
          )
          HeatmapBackButton(
            onClick = { fastFeed = !fastFeed },
            enabled = true,
            modifier = Modifier.padding(start = 12.dp),
            label = if (fastFeed) "Slow feed" else "Fast feed",
          )
        }
        HeatmapBreadcrumb(state = heatmapState, modifier = Modifier.fillMaxWidth())
        Box(Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp)) {
          Heatmap(
            state = heatmapState,
            interaction = HeatmapInteraction(showTooltipOnLongClick = true),
            modifier = Modifier.fillMaxSize(),
            displayPolicy = HeatmapDisplayPolicy(metricFormatter = PercentageMetricFormatter),
            colorScale = colorScale,
            logoContent = { node, size -> CoilHeatmapLogo(node = node, size = size) },
            onLeafClick = { node ->
              scope.launch { Toast.makeText(context, node.label, Toast.LENGTH_SHORT).show() }
            },
          )
        }
      }
    }
  }
}

private enum class DemoDataMode(
  val nextActionLabel: String,
) {
  Normal(nextActionLabel = "5K view"),
  Overview5K(nextActionLabel = "5K raw"),
  Raw5K(nextActionLabel = "Normal"),
  ;

  fun next(): DemoDataMode = when (this) {
    Normal -> Overview5K
    Overview5K -> Raw5K
    Raw5K -> Normal
  }
}

/** Creates a deterministic 5,000-leaf fixture without changing the production repository. */
private fun List<StockItem>.expandForHeatmapStressTest(): List<StockItem> {
  if (isEmpty()) return this
  return List(5_000) { index ->
    val source = this[index % size]
    source.copy(name = "${source.name}-${index + 1}")
  }
}
