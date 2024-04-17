package com.taewooyo.volcano.core.data.di

import com.taewooyo.volcano.core.data.stock.StockRepository
import com.taewooyo.volcano.core.data.stock.StockRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

  @Binds
  fun bindsStockRepository(stockRepositoryImpl: StockRepositoryImpl): StockRepository
}