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
  private val stockRepository: StockRepository
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