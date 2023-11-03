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

import com.example.volcano.tree.Element
import com.example.volcano.tree.Item
import com.example.volcano.tree.Section
import com.example.volcano.tree.Tree
import com.example.volcano.tree.tree
import kotlin.math.roundToInt

public object VolcanoBuilder {

  public fun build(root: VolcanoRoot): Tree<Item> {
    return tree(
      root = Section(
        name = root.name,
        value = root.weight,
        percentage = 1.0,
      ),
    ) {
      root.sections.sortedByDescending { it.weight }
        .forEach { section ->
          node(
            Section(
              name = section.name,
              value = section.weight,
              percentage = (section.elements.sumOf { it.weight } / root.weight),
            ),
          ) {
            section.elements.sortedByDescending { it.weight }
              .forEach { element ->
                node(
                  Element(
                    name = element.name,
                    value = element.weight,
                    percentage = (element.percentage * 100).roundToInt() / 100.0,
                    color = element.color,
                  ),
                )
              }
          }
        }
    }
  }
}
