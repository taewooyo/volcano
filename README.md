<h1 align="center">Volcano</h1></br>

<p align="center">
🌋 Heatmap charts created in an optimized way, fully customizable for Android.
</p>

<br>
<p align="center">
<img src="https://github.com/taewooyo/volcano/assets/66770613/18a319ba-1570-4a11-9f71-e701bcc165da" width="280"/>
<img src="https://github.com/taewooyo/volcano/assets/66770613/90d1a9b7-bdf4-488a-a326-2404f2d7e668" height="280"/>
</p>

## Volcano in Jetpack Compose

If you want to use Volcano in your Jetpack Compose project, check out the **[Volcano in Jetpack Compose](https://github.com/taewooyo/Volcano#volcano-in-jetpack-compose-1)** guidelines.

## How to Use

```kotlin
val totalValue = dummyData.sumOf { it.value }
val dividedDummyData = dummyData.groupBy { it.type }
val volcano = root {
  name { null }
  weight { totalValue }
  sections {
    dividedDummyData.toList().forEach { (type, items) ->
      section {
        name { type.name }
        weight { items.sumOf { it.value } }
        elements {
          items.forEach { hotIssue ->
            element {
              name { hotIssue.name }
              weight { hotIssue.value }
              percentage { (hotIssue.oldValue / hotIssue.value) * 100 }
              color { getColor((hotIssue.oldValue / hotIssue.value) * 100).toLong() }
            }
          }
        }
      }
    }
  }
}
```

### Create Volcano with Kotlin DSL

We can also create an instance of the Volcano with the Kotlin DSL.

## Volcano in Jetpack Compose

Volcano allows you to display heatmap chart in Jetpack Compose easily.

![Maven Central](https://img.shields.io/maven-central/v/io.github.taewooyo/volcano)
![Maven Central](https://img.shields.io/maven-central/v/io.github.taewooyo/volcano-compose)

Add the dependency below to your **module**'s `build.gradle` file

```build.gradle
dependencies {
    implementation "io.github.taewooyo:volcano:${version}"
    implementation "io.github.taewooyo:volcano-compose:${version}"
}
```

### Volcano Composable

You can display heatmap with `Volcano` composable function and `Builder` like the below:

```kotlin
val totalValue = dummyData.sumOf { it.value }
val dividedDummyData = dummyData.groupBy { it.type }
val volcano = root {
  name { null }
  weight { totalValue }
  sections {
    dividedDummyData.toList().forEach { (type, items) ->
      section {
        name { type.name }
        weight { items.sumOf { it.value } }
        elements {
          items.forEach { hotIssue ->
            element {
              name { hotIssue.name }
              weight { hotIssue.value }
              percentage { (hotIssue.oldValue / hotIssue.value) * 100 }
              color { getColor((hotIssue.oldValue / hotIssue.value) * 100).toLong() }
            }
          }
        }
      }
    }
  }
}

Volcano(
  modifier = Modifier,
  items = VolcanoBuilder.build(volcano),
  onClickSection = {},
  onClickElement = {},
  selectedBorderColor = Color.Black,
  selectedItem = null,
  showRateText = true,
)
```

# License

```xml
Copyright 2023 taewooyo

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```
