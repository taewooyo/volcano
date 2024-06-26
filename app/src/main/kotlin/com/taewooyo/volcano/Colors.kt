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
  percentage < -30.0 -> Color(0xFF9a1f29).toArgb()
  percentage < -20.0 -> Color(0xFFf23645).toArgb()
  percentage < -10.0 -> Color(0xFFf77c80).toArgb()
  percentage < 0.0 -> Color(0xFFc2c4cd).toArgb()
  percentage < 10.0 -> Color(0xFF43bd7f).toArgb()
  percentage < 20.0 -> Color(0xFF049950).toArgb()
  percentage < 30.0 -> Color(0xFF076636).toArgb()
  else -> Color(0xFFFC9ABB).toArgb()
}
