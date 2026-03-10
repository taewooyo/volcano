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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
  borderColor: Color = Color.White,
  showRateText: Boolean = false,
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
      selectedBorderColor = selectedBorderColor,
      onClickItem = onClickElement,
      onClickSection = onClickSection,
      borderColor = borderColor,
      showRateText = showRateText,
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
  showRateText: Boolean,
) {
  TreemapChart(
    data = tree,
    evaluateItem = Item::value,
    modifier = modifier,
    borderColor = borderColor,
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
            showRateText = showRateText,
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
  showRateText: Boolean,
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
    val density = LocalDensity.current.density
    
    val minDim = if (boxWidth < boxHeight) boxWidth.toFloat() else boxHeight.toFloat()

    // 1. 아주 극단적으로 작은 박스 (20px 미만) -> 아무것도 안 보여줌
    if (minDim >= 20f) {
      Column(
        modifier = Modifier.fillMaxSize().padding((minDim * 0.02f / density).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // 2. 로고: 20px 이상이면 무조건 표시 (우선순위 1)
        if (item.logoUrl != null) {
           AsyncImage(
             model = item.logoUrl,
             contentDescription = null,
             modifier = Modifier
               .size((minDim * 0.25f / density).dp)
               .background(Color.White.copy(alpha = 0.2f), CircleShape)
               .clip(CircleShape),
             contentScale = ContentScale.Crop
           )
        }

        // 3. 텍스트 영역: 최소폭이 40px 이상일 때만 표시 시도
        if (minDim >= 40f) {
          Spacer(modifier = Modifier.height((minDim * 0.04f / density).dp))
          
          val rateText = "${if (item.percentage > 0) "+" else ""}${item.percentage}%"
          
          // 4. 종목명은 최소폭이 80px 이상일 때만 보여줌 (우선순위 3)
          val finalName = if (minDim >= 80f) item.name ?: "" else ""
          
          AutoSizeText(
            showRateText = showRateText, // 등락률은 우선순위 2 (40px 이상이면 표시)
            name = finalName,
            fluctuateText = rateText,
            minDim = minDim
          )
        }
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
  showRateText: Boolean,
  name: String,
  fluctuateText: String,
  modifier: Modifier = Modifier,
  minDim: Float,
) {
  val density = LocalDensity.current.density
  
  // 폰트 크기 계산 (coerceAtLeast를 낮춰서 더 작은 박스에서도 대응)
  val nameSize = (minDim * 0.14f / density).coerceAtLeast(5f)
  val rateSize = (minDim * 0.12f / density).coerceAtLeast(5f)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    if (name.isNotEmpty()) {
      Text(
        text = name,
        fontSize = nameSize.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        softWrap = false,
        style = androidx.compose.ui.text.TextStyle(
          platformStyle = androidx.compose.ui.text.PlatformTextStyle(
            includeFontPadding = false
          )
        )
      )
    }
    
    if (showRateText) {
      if (name.isNotEmpty()) {
        Spacer(modifier = Modifier.height((minDim * 0.01f / density).dp))
      }
      Text(
        text = fluctuateText,
        fontSize = rateSize.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        softWrap = false,
        style = androidx.compose.ui.text.TextStyle(
          platformStyle = androidx.compose.ui.text.PlatformTextStyle(
            includeFontPadding = false
          )
        )
      )
    }
  }
}

@Composable
internal fun <T> TreemapChart(
  data: Tree<T>,
  evaluateItem: (T) -> Double,
  modifier: Modifier = Modifier,
  borderColor: Color,
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
      borderColor = borderColor,
    )
  }
}

@Composable
internal fun <T> TreemapChartNode(
  data: Tree.Node<T>,
  evaluateItem: (T) -> Double,
  borderColor: Color,
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
      borderColor = borderColor,
    ) { elementNode ->
      TreemapChartNode(
        data = elementNode,
        evaluateItem = evaluateItem,
        nodeContent = nodeContent,
        borderColor = borderColor,
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
  borderColor: Color,
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
            .border(0.5.dp, borderColor)
            .background(borderColor)
            .clickable { onClickSection(sectionName) },
          fontSize = 12.sp,
          color = Color.Black,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
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
