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
package com.taewooyo.volcano.core.data.stock

import com.google.gson.Gson
import com.taewooyo.volcano.core.assets.AssetLoader
import com.taewooyo.volcano.core.model.Stocks
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
  private val assetLoader: AssetLoader,
) : StockRepository {
  override fun fetchStockList(): Stocks? {
    val gson = Gson()
    return assetLoader.getJsonString("stock.json")?.let { jsonString ->
      gson.fromJson(jsonString, Stocks::class.java)
    }
  }
}
