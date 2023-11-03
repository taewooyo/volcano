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
package com.example.volcano.volcano

import VolcanoSection
import VolcanoSectionListBuilder

fun root(builder: VolcanoRoot.Builder.() -> Unit): VolcanoRoot {
  return VolcanoRoot.Builder().apply(builder).build()
}

public data class VolcanoRoot(
  val name: String?,
  val weight: Double,
  val sections: List<VolcanoSection>,
) {

  class Builder {
    private var name: String? = null
    private var weight: Double = 0.0
    private val sections: MutableList<VolcanoSection> = mutableListOf()

    fun name(lambda: () -> String?) {
      name = lambda()
    }

    fun weight(lambda: () -> Double) {
      weight = lambda()
    }

    fun sections(lambda: VolcanoSectionListBuilder.() -> Unit) =
      sections.addAll(VolcanoSectionListBuilder().apply(lambda).build())

    fun build(): VolcanoRoot {
      return VolcanoRoot(name, weight, sections)
    }
  }
}
