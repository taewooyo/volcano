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
package com.example.volcano

import androidx.compose.ui.graphics.Color

private fun getColor(percentage: Double) = when {
  percentage < -3.0 -> Color(0xFFFF0000)
  percentage < -2.0 -> Color(0xFFFF8c00)
  percentage < -1.0 -> Color(0xFFFFFF00)
  percentage < 0.0 -> Color(0xFF008000)
  percentage < 1.0 -> Color(0xFF0000ff)
  percentage < 2.0 -> Color(0xFF4b0082)
  percentage < 3.0 -> Color(0xFF800080)
  else -> Color(0xFF888888)
}
