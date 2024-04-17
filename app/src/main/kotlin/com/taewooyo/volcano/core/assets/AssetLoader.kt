package com.taewooyo.volcano.core.assets

import android.content.Context
import javax.inject.Inject

class AssetLoader @Inject constructor(private val context: Context) {
  fun getJsonString(fileName: String): String? {
    return runCatching {
      loadAsset(fileName)
    }.getOrNull()
  }

  private fun loadAsset(fileName: String): String {
    return context.assets.open(fileName).use { inputStream ->
      val size = inputStream.available()
      val bytes = ByteArray(size)
      inputStream.read(bytes)
      String(bytes)
    }
  }
}