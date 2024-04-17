package com.taewooyo.volcano.core.assets.di

import android.content.Context
import com.taewooyo.volcano.core.assets.AssetLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AssetModule {

  @Provides
  @Singleton
  fun provideAssetLoader(@ApplicationContext context: Context): AssetLoader = AssetLoader(context)
}