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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.volcano.compose.Volcano
import com.example.volcano.volcano.VolcanoBuilder
import com.example.volcano.volcano.root

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val totalValue = dummyData.sumOf { it.value }
    val volcano = root {
      name { "Volcano" }
      weight { totalValue }
      sections {
        section {
          name { "GDP Total" }
          weight { totalValue }
          elements {
            dummyData.forEach { gdp ->
              element {
                name { gdp.name }
                weight { gdp.value }
                percentage { (gdp.oldValue / gdp.value) * 100 }
                color { getColor((gdp.oldValue / gdp.value) * 100).toLong() }
              }
            }
          }
        }
      }
    }

    setContent {
      Box(modifier = Modifier.fillMaxSize()) {
        Volcano(
          modifier = Modifier,
          items = VolcanoBuilder.build(volcano),
          onClickSection = {},
          onClickElement = {},
          selectedBorderColor = Color.Black,
          selectedItem = null,
        )
      }
    }
  }
}
