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
import com.example.volcano.volcano.VolcanoElement
import com.example.volcano.volcano.VolcanoElementListBuilder

public data class VolcanoSection(
  val name: String?,
  val weight: Double,
  val elements: List<VolcanoElement>,
) {

  class Builder {
    private var name: String? = null
    private var weight: Double = 0.0
    private val elements: MutableList<VolcanoElement> = mutableListOf()

    fun name(lambda: () -> String?) {
      name = lambda()
    }

    fun weight(lambda: () -> Double) {
      weight = lambda()
    }

    fun elements(lambda: VolcanoElementListBuilder.() -> Unit) =
      elements.addAll(VolcanoElementListBuilder().apply(lambda).build())

    fun build(): VolcanoSection {
      return VolcanoSection(name, weight, elements)
    }
  }
}

class VolcanoSectionListBuilder {

  private val sectionList: MutableList<VolcanoSection> = mutableListOf()

  fun section(lambda: VolcanoSection.Builder.() -> Unit) =
    sectionList.add(VolcanoSection.Builder().apply(lambda).build())

  fun build() = sectionList
}
