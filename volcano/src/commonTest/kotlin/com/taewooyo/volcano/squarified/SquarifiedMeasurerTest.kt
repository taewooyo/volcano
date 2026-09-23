/*
 * Copyright (C) 2026 taewooyo
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
package com.taewooyo.volcano.squarified

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SquarifiedMeasurerTest {

  @Test
  fun `measures every value inside the requested bounds`() {
    val nodes = SquarifiedMeasurer().measureNodes(
      values = listOf(6.0, 3.0, 1.0),
      width = 300,
      height = 200,
    )

    assertEquals(3, nodes.size)
    assertEquals(
      listOf(
        listOf(0, 0, 180, 200),
        listOf(180, 0, 120, 150),
        listOf(180, 150, 120, 50),
      ),
      nodes.map { node -> listOf(node.offsetX, node.offsetY, node.width, node.height) },
    )
    nodes.forEach { node ->
      assertTrue(node.width >= 0)
      assertTrue(node.height >= 0)
      assertTrue(node.offsetX >= 0)
      assertTrue(node.offsetY >= 0)
      assertTrue(node.offsetX + node.width <= 300)
      assertTrue(node.offsetY + node.height <= 200)
    }
  }

  @Test
  fun `measures dense feeds without recursive stack growth`() {
    val nodes = SquarifiedMeasurer().measureNodes(
      values = List(5_000) { index -> (index % 20 + 1).toDouble() },
      width = 1_200,
      height = 800,
    )

    assertEquals(5_000, nodes.size)
    nodes.forEach { node ->
      assertTrue(node.width >= 0)
      assertTrue(node.height >= 0)
      assertTrue(node.offsetX >= 0)
      assertTrue(node.offsetY >= 0)
      assertTrue(node.offsetX + node.width <= 1_200)
      assertTrue(node.offsetY + node.height <= 800)
    }
  }
}
