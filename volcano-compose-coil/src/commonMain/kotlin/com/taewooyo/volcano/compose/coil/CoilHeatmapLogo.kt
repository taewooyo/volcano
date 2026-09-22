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
package com.taewooyo.volcano.compose.coil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.taewooyo.volcano.heatmap.HeatmapNode

/**
 * Coil-backed logo content for [com.taewooyo.volcano.compose.Heatmap].
 *
 * A label initial remains visible while the image is loading or if its URL cannot be loaded.
 */
@Composable
@Suppress("ktlint:standard:function-naming")
public fun CoilHeatmapLogo(node: HeatmapNode, size: Dp, modifier: Modifier = Modifier) {
  val imageUrl = node.imageUrl?.takeIf(String::isNotBlank) ?: return
  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(Color(0xFF1E293B)),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = node.label.take(1).uppercase(),
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = (size.value * 0.42f).sp,
    )
    AsyncImage(
      model = imageUrl,
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
  }
}
