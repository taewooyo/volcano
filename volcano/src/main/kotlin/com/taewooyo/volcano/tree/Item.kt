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
package com.taewooyo.volcano.tree

interface Item {
  public val name: String?
  public val value: Double
  public val percentage: Double
}

public data class Element internal constructor(
  override val name: String?,
  override val value: Double,
  override val percentage: Double,
  val color: Long,
) : Item

public data class Section internal constructor(
  override val name: String?,
  override val value: Double,
  override val percentage: Double,
) : Item
