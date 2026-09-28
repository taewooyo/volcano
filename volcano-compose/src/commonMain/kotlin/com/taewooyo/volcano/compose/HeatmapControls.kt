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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

/** A small platform-neutral navigation control for heatmap sample chrome. */
@Composable
public fun HeatmapBackButton(
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  label: String? = null,
) {
  val labels = LocalHeatmapLabels.current
  val buttonLabel = label ?: labels.back
  val shape = RoundedCornerShape(10.dp)
  Text(
    text = buttonLabel,
    modifier = modifier
      .clip(shape)
      .background(if (enabled) Color(0xFFF1F5F9) else Color(0xFFE5E7EB))
      .border(1.dp, if (enabled) Color(0xFFD5DCE5) else Color.Transparent, shape)
      .semantics { stateDescription = if (enabled) labels.enabled else labels.disabled }
      .clickable(
        enabled = enabled,
        onClickLabel = buttonLabel,
        role = Role.Button,
        onClick = onClick,
      )
      .padding(horizontal = 14.dp, vertical = 8.dp),
    color = if (enabled) Color(0xFF1F2937) else Color(0xFF94A3B8),
  )
}
