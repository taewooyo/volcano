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
package com.example.volcano

data class GDP(
  val name: String,
  val value: Double,
  val oldValue: Double,
)

val dummyData = listOf<GDP>(
  GDP(
    name = "USA",
    value = 26_949_643.0,
    oldValue = 1_485_168.0,
  ),
  GDP(
    name = "China",
    value = 17_700_899.0,
    oldValue = -399_145.0,
  ),
  GDP(
    name = "Germany",
    value = 4_429_838.0,
    oldValue = 354_443.0,
  ),
  GDP(
    name = "JAPAN",
    value = 4230862.0,
    oldValue = -2_676.0,
  ),
  GDP(
    name = "India",
    value = 3_732_224.0,
    oldValue = 345_821.0,
  ),
  GDP(
    name = "UK",
    value = 3_332_059.0,
    oldValue = 261_459.0,
  ),
  GDP(
    name = "France",
    value = 3_049_016.0,
    oldValue = 264_996.0,
  ),
  GDP(
    name = "Italy",
    value = 2_186_082.0,
    oldValue = 174_069.0,
  ),
  GDP(
    name = "Brazil",
    value = 2_126_809.0,
    oldValue = 202_675.0,
  ),
  GDP(
    name = "Canada",
    value = 2_117_805.0,
    oldValue = -22_035.0,
  ),
  GDP(
    name = "Russia",
    value = 1_862_470.0,
    oldValue = -377_952.0,
  ),
  GDP(
    name = "Mexico",
    value = 1_811_468.0,
    oldValue = 386_935.0,
  ),
  GDP(
    name = "Korea",
    value = 1_709_232.0,
    oldValue = 43_986.0,
  ),
  GDP(
    name = "Australia",
    value = 1_687_713.0,
    oldValue = -14_180.0,
  ),
  GDP(
    name = "Spain",
    value = 1_582_054.0,
    oldValue = 192_127.0,
  ),
  GDP(
    name = "Indonesia",
    value = 1_417_387.0,
    oldValue = 127_958.0,
  ),
  GDP(
    name = "Turkye",
    value = 1_154_600.0,
    oldValue = 301_113.0,
  ),
  GDP(
    name = "Netherlands",
    value = 1_092_748.0,
    oldValue = 102_165.0,
  ),
  GDP(
    name = "Saudi Arabia",
    value = 1_069_437.0,
    oldValue = 58_849.0,
  ),
  GDP(
    name = "Swiss",
    value = 905_684.0,
    oldValue = 98_450.0,
  ),
)
