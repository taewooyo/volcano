package com.taewooyo.volcano.core.data.stock

import com.taewooyo.volcano.core.model.Stocks
import kotlinx.coroutines.flow.Flow

interface StockRepository {

  fun fetchStockList(): Stocks?
}