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
package com.taewooyo.volcano.volcano

public data class VolcanoElement internal constructor(
  val name: String,
  val weight: Double,
  val color: Long,
  val percentage: Double,
) {

  class Builder internal constructor() {
    private var name: String = ""
    private var weight: Double = 0.0
    private var color: Long = 0xFF000000
    private var percentage: Double = 0.0

    fun name(lambda: () -> String) {
      name = lambda()
    }

    fun weight(lambda: () -> Double) {
      weight = lambda()
    }

    fun color(lambda: () -> Long) {
      color = lambda()
    }

    fun percentage(lambda: () -> Double) {
      percentage = lambda()
    }

    fun build(): VolcanoElement {
      require(name != "") { "You need to enter the name value. Don't enter the Blank" }
      return VolcanoElement(name, weight, color, percentage)
    }
  }
}

class VolcanoElementListBuilder internal constructor() {

  private val elementList: MutableList<VolcanoElement> = mutableListOf()

  fun element(lambda: VolcanoElement.Builder.() -> Unit) =
    elementList.add(VolcanoElement.Builder().apply(lambda).build())

  fun build() = elementList
}
