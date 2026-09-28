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
package com.taewooyo.volcano.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/** User-facing defaults. Override through [LocalHeatmapLabels] for localization. */
public data class HeatmapLabels(
  val noData: String = "No data",
  val selected: String = "Selected",
  val notSelected: String = "Not selected",
  val currentGroup: String = "Current group",
  val openGroup: String = "Open group",
  val navigate: String = "Navigate to",
  val showDetails: String = "Show details",
  val metric: String = "metric",
  val enabled: String = "Enabled",
  val disabled: String = "Disabled",
  val back: String = "Back",
  val value: String = "value",
)

public val LocalHeatmapLabels: ProvidableCompositionLocal<HeatmapLabels> =
  staticCompositionLocalOf { HeatmapLabels() }
