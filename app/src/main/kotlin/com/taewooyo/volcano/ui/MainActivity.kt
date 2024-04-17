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
package com.taewooyo.volcano.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.taewooyo.volcano.compose.Volcano
import com.taewooyo.volcano.getColor
import com.taewooyo.volcano.volcano.VolcanoBuilder
import com.taewooyo.volcano.volcano.root
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val stocks = viewModel.stocks.collectAsState().value
      val total = stocks.stocks.sumOf { it.value }
      val sector = stocks.stocks.groupBy { it.type }

      val volcano = root {
        name { null }
        weight { total }
        sections {
          sector.toList().forEach { (type, items) ->
            section {
              name { type }
              weight { items.sumOf { it.value } }
              elements {
                items.forEach { stock ->
                  element {
                    name { stock.name }
                    weight { stock.value }
                    percentage { (stock.oldValue / stock.value) * 100 }
                    color { getColor((stock.oldValue / stock.value) * 100).toLong() }
                  }
                }
              }
            }
          }
        }
      }

      Box(modifier = Modifier.fillMaxSize()) {
        Volcano(
          modifier = Modifier,
          items = VolcanoBuilder.build(volcano),
          onClickSection = {},
          onClickElement = {},
          selectedBorderColor = Color.Black,
          selectedItem = null,
          showRateText = true,
        )
      }
    }
  }
}
