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
package com.taewooyo.volcano

internal enum class Type {
  ENTERTAINMENT,
  OTT,
  SPORT,
  E_SPORT,
}

internal data class HotIssue(
  val name: String,
  val value: Double,
  val oldValue: Double,
  val type: Type,
)

internal val dummyData = listOf<HotIssue>(
  HotIssue(
    name = "BTS",
    value = 26_949_643.0,
    oldValue = 1_485_168.0,
    type = Type.ENTERTAINMENT,
  ),
  HotIssue(
    name = "Black Pink",
    value = 17_700_899.0,
    oldValue = -399_145.0,
    type = Type.ENTERTAINMENT,
  ),
  HotIssue(
    name = "NETFLIX",
    value = 4_429_838.0,
    oldValue = 354_443.0,
    type = Type.OTT,
  ),
  HotIssue(
    name = "Coupang Play",
    value = 4230862.0,
    oldValue = -2_676.0,
    type = Type.OTT,
  ),
  HotIssue(
    name = "TVING",
    value = 3_732_224.0,
    oldValue = 345_821.0,
    type = Type.OTT,
  ),
  HotIssue(
    name = "Wavve",
    value = 3_332_059.0,
    oldValue = 261_459.0,
    type = Type.OTT,
  ),
  HotIssue(
    name = "LOL",
    value = 3_049_016.0,
    oldValue = 264_996.0,
    type = Type.E_SPORT,
  ),
  HotIssue(
    name = "Valorant",
    value = 2_186_082.0,
    oldValue = 174_069.0,
    type = Type.E_SPORT,
  ),
  HotIssue(
    name = "Disney+",
    value = 2_126_809.0,
    oldValue = 202_675.0,
    type = Type.OTT,
  ),
  HotIssue(
    name = "G-Dragon",
    value = 2_117_805.0,
    oldValue = -22_035.0,
    type = Type.ENTERTAINMENT,
  ),
  HotIssue(
    name = "Overwatch",
    value = 1_862_470.0,
    oldValue = -377_952.0,
    type = Type.E_SPORT,
  ),
  HotIssue(
    name = "Son Heung min",
    value = 1_709_232.0,
    oldValue = 43_986.0,
    type = Type.SPORT,
  ),
  HotIssue(
    name = "Cristiano Ronaldo",
    value = 1_582_054.0,
    oldValue = 192_127.0,
    type = Type.SPORT,
  ),
  HotIssue(
    name = "Lionell Messi",
    value = 1_417_387.0,
    oldValue = 127_958.0,
    type = Type.SPORT,
  ),
  HotIssue(
    name = "Fortnite",
    value = 1_092_748.0,
    oldValue = 102_165.0,
    type = Type.E_SPORT,
  ),
  HotIssue(
    name = "Battle Ground",
    value = 905_684.0,
    oldValue = 98_450.0,
    type = Type.E_SPORT,
  ),
)
