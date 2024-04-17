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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taewooyo.volcano.core.data.stock.StockRepository
import com.taewooyo.volcano.core.model.Stocks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val stockRepository: StockRepository,
) : ViewModel() {

  private val _stocks: MutableStateFlow<Stocks> = MutableStateFlow(value = Stocks(emptyList()))
  val stocks = _stocks.asStateFlow()

  init {
    viewModelScope.launch {
      val stocks = stockRepository.fetchStockList()
      _stocks.update { state ->
        stocks?.let {
          state.copy(stocks = it.stocks)
        } ?: state
      }
    }
  }
}
