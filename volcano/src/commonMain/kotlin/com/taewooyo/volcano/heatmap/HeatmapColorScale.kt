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

/** Maps a node to an ARGB color without depending on a UI toolkit. */
public fun interface HeatmapColorScale {
  public fun colorOf(node: HeatmapNode): Long
}

/** One labelled color used by platform-neutral heatmap legends. */
public data class HeatmapLegendEntry(
  val label: String,
  val color: Long,
)

/**
 * A signed-metric red/neutral/green scale with linear intensity within [maximumAbsoluteMetric].
 *
 * Explicit [HeatmapNode.color] values always take precedence when a heatmap is rendered.
 */
public data class SignedMetricColorScale(
  val maximumAbsoluteMetric: Double = 10.0,
  val negative: Long = 0xFFD44848,
  val neutral: Long = 0xFF6B7280,
  val positive: Long = 0xFF1E9E63,
) : HeatmapColorScale {

  init {
    require(maximumAbsoluteMetric.isFinite() && maximumAbsoluteMetric > 0.0) {
      "maximumAbsoluteMetric must be a positive finite value."
    }
  }

  override fun colorOf(node: HeatmapNode): Long {
    val metric = node.effectiveMetric ?: return neutral
    if (metric == 0.0) return neutral
    val target = if (metric > 0.0) positive else negative
    val intensity = (kotlin.math.abs(metric) / maximumAbsoluteMetric).coerceIn(0.0, 1.0)
    return interpolate(neutral, target, intensity)
  }
}

/** Default legend entries for [SignedMetricColorScale]. */
public fun SignedMetricColorScale.legendEntries(): List<HeatmapLegendEntry> = listOf(
  HeatmapLegendEntry(label = "-${maximumAbsoluteMetric}", color = negative),
  HeatmapLegendEntry(label = "0", color = neutral),
  HeatmapLegendEntry(label = "+${maximumAbsoluteMetric}", color = positive),
)

private fun interpolate(start: Long, end: Long, fraction: Double): Long {
  fun channel(color: Long, shift: Int): Int = ((color shr shift) and 0xFF).toInt()
  fun mix(shift: Int): Long = (
    channel(start, shift) + ((channel(end, shift) - channel(start, shift)) * fraction)
  ).toInt().toLong()

  return (mix(24) shl 24) or (mix(16) shl 16) or (mix(8) shl 8) or mix(0)
}
