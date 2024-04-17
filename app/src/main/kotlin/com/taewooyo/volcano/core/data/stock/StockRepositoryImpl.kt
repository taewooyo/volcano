package com.taewooyo.volcano.core.data.stock

import com.google.gson.Gson
import com.taewooyo.volcano.core.assets.AssetLoader
import com.taewooyo.volcano.core.model.Stocks
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
  private val assetLoader: AssetLoader
) : StockRepository {
  override fun fetchStockList(): Stocks? {
    val gson = Gson()
    return assetLoader.getJsonString("stock.json")?.let { jsonString ->
      gson.fromJson(jsonString, Stocks::class.java)
    }
  }
}