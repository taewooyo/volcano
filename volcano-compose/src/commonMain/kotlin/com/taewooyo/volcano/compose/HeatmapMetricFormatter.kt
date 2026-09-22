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

import kotlin.math.abs
import kotlin.math.roundToLong

/** Formats a node's signed metric for the default heatmap cell. */
public fun interface HeatmapMetricFormatter {
  public fun format(metric: Double): String
}

/** Default domain-neutral metric formatter. */
public object SignedMetricFormatter : HeatmapMetricFormatter {
  override fun format(metric: Double): String = "${if (metric > 0.0) "+" else ""}$metric"
}

/** Convenience formatter for metrics expressed as percentages. */
public object PercentageMetricFormatter : HeatmapMetricFormatter {
  override fun format(metric: Double): String {
    val hundredths = (metric * 100.0).roundToLong()
    val magnitude = abs(hundredths)
    val sign = when {
      hundredths > 0L -> "+"
      hundredths < 0L -> "-"
      else -> ""
    }
    return "$sign${magnitude / 100}.${(magnitude % 100).toString().padStart(2, '0')}%"
  }
}
