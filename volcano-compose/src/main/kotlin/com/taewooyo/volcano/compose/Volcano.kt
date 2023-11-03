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
package com.taewooyo.volcano.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.taewooyo.volcano.tree.Element
import com.taewooyo.volcano.tree.Item
import com.taewooyo.volcano.tree.Section
import com.taewooyo.volcano.tree.Tree
import kotlin.math.abs

@Composable
public fun Volcano(
  modifier: Modifier = Modifier,
  items: Tree<Item>,
  selectedItem: Item? = null,
  selectedBorderColor: Color = Color.White,
  onClickElement: (element: Element) -> Unit = {},
  onClickSection: (sectionName: String?) -> Unit = {},
  borderColor: Color = Color.Black,
) {
  val minScale = 1f
  val maxScale = 10f
  var scale by remember { mutableFloatStateOf(minScale) }
  var lastScale by remember { mutableFloatStateOf(0f) }
  var pointX = 0f
  var pointY = 0f
  var wholeWidthLength by remember { mutableIntStateOf(0) }
  var wholeHeightLength by remember { mutableIntStateOf(0) }
  var offsetXValue by remember { mutableFloatStateOf(0f) }
  var offsetYValue by remember { mutableFloatStateOf(0f) }
  val localDensity = LocalDensity.current

  Box(
    modifier
      .fillMaxSize()
      .zIndex(-1f)
      .background(borderColor)
      .onSizeChanged {
        wholeWidthLength = it.width - 5.dp
          .toPx(localDensity)
          .toInt()
        wholeHeightLength = it.height - 5.dp
          .toPx(localDensity)
          .toInt()
      }
      .pointerInput(Unit) {
        awaitEachGesture {
          do {
            val event = awaitPointerEvent()
            scale *= event.calculateZoom()
            scale = maxOf(minScale, minOf(maxScale, scale))

            val offset = event.calculatePan()
            if (pointX == 0f && pointY == 0f) {
              pointX = offset.x
              pointY = offset.y
            }
            val isDragging =
              abs(x = pointX - offset.x) > 25 || abs(x = pointY - offset.y) > 25

            val isScaling = scale != lastScale
            lastScale = scale
            if (offset == Offset(0f, 0f)) {
              pointX = 0f
              pointY = 0f
            }

            val width = (wholeWidthLength / 2) * (scale - 1f)
            offsetXValue = (offsetXValue + offset.x)
              .coerceAtLeast(width * 1.05f * -1)
              .coerceAtMost(width)

            val height = (wholeHeightLength / 2) * (scale - 1f)
            offsetYValue = (offsetYValue + offset.y)
              .coerceAtLeast(height * 1.1f * -1)
              .coerceAtMost(height)
          } while (
            event.changes.any {
              if (isScaling || isDragging) {
                it.consume()
              }
              it.pressed
            }
          )
        }
      }
      .graphicsLayer(
        scaleX = scale,
        scaleY = scale,
        translationX = offsetXValue,
        translationY = offsetYValue,
      )
      .animateContentSize(),
  ) {
    InternalTreemapChart(
      tree = items,
      selectedItem = selectedItem,
      selectedBorderColor,
      onClickItem = onClickElement,
      onClickSection = onClickSection,
      borderColor = borderColor,
    )
  }
}

@Composable
private fun InternalTreemapChart(
  tree: Tree<Item>,
  selectedItem: Item?,
  selectedBorderColor: Color,
  modifier: Modifier = Modifier,
  onClickItem: (Element) -> Unit,
  onClickSection: (String?) -> Unit,
  borderColor: Color,
) {
  TreemapChart(
    data = tree,
    evaluateItem = Item::value,
    modifier = modifier,
  ) { node, groupContent ->
    when (val item = node.data) {
      is Section -> {
        Section { groupContent(node, item.name, onClickSection) }
      }

      is Element -> {
        if (node.elements.isEmpty()) {
          Element(
            item = item,
            selectedItem = selectedItem as? Element,
            selectedBorderColor = selectedBorderColor,
            onClick = onClickItem,
            borderColor = borderColor,
          )
        }
      }
    }
  }
}

@Composable
private fun Element(
  item: Element,
  selectedItem: Element?,
  selectedBorderColor: Color,
  modifier: Modifier = Modifier,
  onClick: (Element) -> Unit,
  borderColor: Color,
) {
  BoxWithConstraints(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .clickable { onClick(item) }
      .border(
        0.5.dp,
        if (item.name == selectedItem?.name) selectedBorderColor else borderColor,
      )
      .background(Color(item.color)),
  ) {
    val boxWidth = constraints.maxWidth
    val boxHeight = constraints.maxHeight
    if (boxWidth >= 20 && boxHeight >= 20) {
      item.name?.let {
        AutoSizeText(
          stockText = it,
          fluctuateText = "${item.percentage}%",
          width = with(LocalDensity.current) { boxWidth.toFloat() },
          height = with(LocalDensity.current) { boxHeight.toFloat() },
        )
      }
    }
  }
}

@Composable
private fun Section(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier
      .padding(0.5.dp),
  ) {
    content()
  }
}

@Composable
internal fun AutoSizeText(
  stockText: String,
  fluctuateText: String,
  modifier: Modifier = Modifier,
  width: Float,
  height: Float,
) {
  val nameLength = stockText.length
  val fluctuation = fluctuateText.length

  val widthBaseTextSize = (
    (width / nameLength)
      .coerceAtMost(width / fluctuation) / LocalDensity.current.density
    )
    .coerceAtLeast(minTextSize)

  val heightBaseTextSize = ((height * textLineSpace - 15) / 2 / LocalDensity.current.density)
    .coerceAtLeast(minTextSize)

  val length = widthBaseTextSize.coerceAtMost(heightBaseTextSize)

  Column(
    modifier = Modifier
      .padding(horizontal = 2.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stockText,
      modifier = modifier,
      fontSize = length.sp,
      fontWeight = FontWeight.W600,
      letterSpacing = if (length < 1f && width * 2f > height) 0.2.sp else 0.1.sp,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      color = Color.White,
      textAlign = TextAlign.Center,
      softWrap = false,
    )
    Text(
      text = fluctuateText,
      modifier = modifier,
      fontSize = length.sp,
      fontWeight = FontWeight.W600,
      letterSpacing = if (length < 1f && width * 2f > height) 0.2.sp else 0.1.sp,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      color = Color.White,
      textAlign = TextAlign.Center,
      softWrap = false,
    )
  }
}

private const val textLineSpace = 0.9f
private const val minTextSize = 0.8f

@Composable
internal fun <T> TreemapChart(
  data: Tree<T>,
  evaluateItem: (T) -> Double,
  modifier: Modifier = Modifier,
  nodeContent: @Composable (
    data: Tree.Node<T>,
    groupContent: @Composable (Tree.Node<T>, String?, (String?) -> Unit) -> Unit,
  ) -> Unit,
) {
  Box(modifier) {
    TreemapChartNode(
      data = data.root,
      evaluateItem = evaluateItem,
      nodeContent = nodeContent,
    )
  }
}

@Composable
internal fun <T> TreemapChartNode(
  data: Tree.Node<T>,
  evaluateItem: (T) -> Double,
  nodeContent: @Composable (
    data: Tree.Node<T>,
    groupContent: @Composable (Tree.Node<T>, String?, (String?) -> Unit) -> Unit,
  ) -> Unit,
) {
  nodeContent(data) { node, sectionName, onClickSection ->
    TreemapChartLayout(
      data = node,
      sectionName = sectionName,
      evaluateItem = evaluateItem,
      onClickSection = onClickSection,
    ) { elementNode ->
      TreemapChartNode(
        data = elementNode,
        evaluateItem = evaluateItem,
        nodeContent = nodeContent,
      )
    }
  }
}

@Composable
internal fun <T> TreemapChartLayout(
  data: Tree.Node<T>,
  sectionName: String?,
  evaluateItem: (T) -> Double,
  modifier: Modifier = Modifier,
  onClickSection: (String?) -> Unit,
  itemContent: @Composable (Tree.Node<T>) -> Unit,
) {
  val treemapChartMeasurer = LocalVolcanoMeasurer.current
  Layout(
    content = {
      if (sectionName != null) {
        Text(
          text = sectionName,
          modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.Black)
            .background(Color(0xFF212529))
            .clickable { onClickSection(sectionName) },
          fontSize = 12.sp,
          color = Color.White,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          textAlign = TextAlign.Center,
        )
      }
      data.elements.forEach { node ->
        itemContent(node)
      }
    },
    modifier = modifier,
  ) { measurables, constraints ->
    val sectionTextPlaceable = sectionName?.let { measurables.first().measure(constraints) }
    val nodes = treemapChartMeasurer.measureNodes(
      data.elements.map { evaluateItem(it.data) },
      constraints.maxWidth,
      constraints.maxHeight - (sectionTextPlaceable?.height ?: 0),
    )
    val placeables = if (sectionName != null) {
      measurables.drop(1).mapIndexed { index, measurable ->
        measurable.measure(Constraints.fixed(nodes[index].width, nodes[index].height))
      }
    } else {
      measurables.mapIndexed { index, measurable ->
        measurable.measure(Constraints.fixed(nodes[index].width, nodes[index].height))
      }
    }
    layout(constraints.maxWidth, constraints.maxHeight) {
      sectionTextPlaceable?.placeRelative(0, 0)
      placeables.forEachIndexed { index, placeable ->
        placeable.placeRelative(
          nodes[index].offsetX,
          nodes[index].offsetY + (sectionTextPlaceable?.height ?: 0),
        )
      }
    }
  }
}
