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

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taewooyo.volcano.heatmap.HeatmapLegendEntry

/** A compact, platform-independent legend for a heatmap color scale. */
@Composable
public fun HeatmapLegend(
  entries: List<HeatmapLegendEntry>,
  modifier: Modifier = Modifier,
  indicatorSize: Dp = 12.dp,
  contentColor: Color = Color(0xFF252525),
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    entries.forEachIndexed { index, entry ->
      if (index > 0) Spacer(Modifier.width(12.dp))
      Spacer(
        Modifier
          .size(indicatorSize)
          .clip(RoundedCornerShape(3.dp))
          .background(Color(entry.color.toInt())),
      )
      Spacer(Modifier.width(4.dp))
      Text(
        text = entry.label,
        modifier = Modifier.padding(vertical = 2.dp),
        color = contentColor,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
