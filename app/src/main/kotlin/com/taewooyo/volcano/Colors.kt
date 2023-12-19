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
package com.taewooyo.volcano

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

internal fun getColor(percentage: Double): Int = when {
  percentage < -30.0 -> Color(0xFFFCB9AA).toArgb()
  percentage < -25.0 -> Color(0xFFFFDBCC).toArgb()
  percentage < -20.0 -> Color(0xFFECEAE4).toArgb()
  percentage < -15.0 -> Color(0xFFA2E1DB).toArgb()
  percentage < -10.0 -> Color(0xFF55CBCD).toArgb()
  percentage < -5.0 -> Color(0xFFABDEE6).toArgb()
  percentage < 0.0 -> Color(0xFFCBAACB).toArgb()
  percentage < 5.0 -> Color(0xFFE4AE9D).toArgb()
  percentage < 10.0 -> Color(0xFFE1CCB6).toArgb()
  percentage < 15.0 -> Color(0xFFF3B0C3).toArgb()
  percentage < 20.0 -> Color(0xFF4b8f82).toArgb()
  percentage < 25.0 -> Color(0xFFC08863).toArgb()
  percentage < 30.0 -> Color(0xFF5AA08D).toArgb()
  percentage < 35.0 -> Color(0xFFEF84C1).toArgb()
  else -> Color(0xFFFC9ABB).toArgb()
}
