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

import androidx.compose.ui.graphics.Color

/** Visual defaults used by [Heatmap] and [DefaultHeatmapCell]. */
public data class HeatmapStyle(
  val borderColor: Color = Color.White,
  val groupHeaderColor: Color = Color.White,
  val groupHeaderTextColor: Color = Color(0xFF252525),
  /** Optional persistent selection border. The default relies on transient press feedback instead. */
  val selectedBorderColor: Color = Color.Transparent,
  val leafTextColor: Color = Color.White,
)

/** Defines how a [Heatmap] updates [HeatmapState] for its built-in tap targets. */
public data class HeatmapInteraction(
  val drillDownOnGroupClick: Boolean = true,
  val selectLeafOnClick: Boolean = true,
  /** Enables the built-in, transient long-press tooltip. */
  val showTooltipOnLongClick: Boolean = false,
  /** Duration for the built-in tooltip. A value of zero keeps it visible until dismissed. */
  val tooltipDurationMillis: Int = 2_000,
  /** Enables a pointer-hover tooltip on platforms that provide pointer enter and exit events. */
  val showTooltipOnHover: Boolean = false,
  /** Pointer dwell time before the built-in hover tooltip is shown. */
  val tooltipHoverDelayMillis: Int = 400,
) {

  init {
    require(tooltipDurationMillis >= 0) { "tooltipDurationMillis must not be negative." }
    require(tooltipHoverDelayMillis >= 0) { "tooltipHoverDelayMillis must not be negative." }
  }
}

/** Visual transition settings used when the visible drill-down level changes. */
public data class HeatmapMotion(
  val enabled: Boolean = true,
  val durationMillis: Int = 180,
  val initialScale: Float = 0.98f,
  val pressScale: Float = 0.97f,
  val pressedAlpha: Float = 0.92f,
  val pressDurationMillis: Int = 90,
) {

  init {
    require(durationMillis >= 0) { "durationMillis must not be negative." }
    require(initialScale > 0f && initialScale <= 1f) {
      "initialScale must be in the range (0, 1]."
    }
    require(pressScale > 0f && pressScale <= 1f) {
      "pressScale must be in the range (0, 1]."
    }
    require(pressedAlpha > 0f && pressedAlpha <= 1f) {
      "pressedAlpha must be in the range (0, 1]."
    }
    require(pressDurationMillis >= 0) { "pressDurationMillis must not be negative." }
  }
}
