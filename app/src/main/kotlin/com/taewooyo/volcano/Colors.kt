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
  percentage < -30.0 -> Color(0xFFFF0000).toArgb()
  percentage < -25.0 -> Color(0xFFFF4300).toArgb()
  percentage < -20.0 -> Color(0xFF00f0ff).toArgb()
  percentage < -15.0 -> Color(0xFFFF8ff0).toArgb()
  percentage < -10.0 -> Color(0xFFFFFF00).toArgb()
  percentage < -5.0 -> Color(0xFFFF3Ff0).toArgb()
  percentage < 0.0 -> Color(0xFFFF4300).toArgb()
  percentage < 5.0 -> Color(0xFF009f00).toArgb()
  percentage < 10.0 -> Color(0xFF0080ff).toArgb()
  percentage < 15.0 -> Color(0xFF0000ff).toArgb()
  percentage < 20.0 -> Color(0xFF4b8f82).toArgb()
  percentage < 25.0 -> Color(0xFF4b0082).toArgb()
  percentage < 30.0 -> Color(0xFF800080).toArgb()
  percentage < 35.0 -> Color(0xFF51243f).toArgb()
  else -> Color(0xFF888888).toArgb()
}
