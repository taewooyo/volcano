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
package com.example.volcano.squarified

import com.example.volcano.configuration.LayoutOrientation
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SquarifiedMeasurer : Measurer {

  private var height = 0.0 // 차트의 높이
  private var width = 0.0 // 차트의 너비
  private var heightLeft = 0.0 // 남은 높이 추적
  private var widthLeft = 0.0 // 남은 너비 추적
  private var left = 0.0 // 현재 열 혹은 행의 시작점 x position
  private var top = 0.0 // 현재 열 혹은 행의 시작점 y position
  private var layoutOrientation = LayoutOrientation.VERTICAL // 레이아웃 방향
  private var elements = mutableListOf<TreemapElement>() // 차트의 요소 리스트

  /**
   * 차트의 요소들 사이즈 측정 및 설정하는 함수
   * @param [values] 비중 값
   * @param [width] 차트의 가로 길이
   * @param [height] 차트의 세로 길이
   * @return 노드들의 가로, 세로, 오프셋 리스트 반환
   */
  override fun measureNodes(values: List<Double>, width: Int, height: Int): List<TreemapNode> {
    setSizeAndValues(values, width.toDouble(), height.toDouble())
    return measureNodes()
  }

  /**
   * 차트의 사이즈 설정 및 비중 설정하는 함수
   * @param [values] 비중 값
   * @param [width] 차트의 가로 길이
   * @param [height] 차트의 세로 길이
   */
  private fun setSizeAndValues(values: List<Double>, width: Double, height: Double) {
    this.width = width
    this.height = height
    left = 0.0
    top = 0.0
    this.elements.clear()

    for (value in values) {
      val treemapElement = TreemapElement(value)
      this.elements.add(treemapElement)
    }

    layoutOrientation =
      if (width > height) LayoutOrientation.VERTICAL else LayoutOrientation.HORIZONTAL
    scaleArea(this.elements)
  }

  /**
   * 차트의 노드들의 좌표 및 크기를 반환해주는 함수
   * @return 차트맵 배치 알고리즘을 통해 좌표 및 크기가 설정된 차트 노드 리스트를 반환
   */
  private fun measureNodes(): List<TreemapNode> {
    val treemapNodes: MutableList<TreemapNode> = ArrayList()
    heightLeft = height
    widthLeft = width
    top = 0.0
    squarify(ArrayList(elements), ArrayList(), minimumSide())

    for (element in elements) {
      val treemapNode = TreemapNode(
        element.width.toInt(),
        element.height.toInt(),
        element.left.toInt(),
        element.top.toInt(),
      )
      treemapNodes.add(treemapNode)
    }
    return treemapNodes
  }

  /**
   * @param [elements] 가중치
   * @param [row] 현재 행
   * @param [w] 그려질 수 있는 영역의 남은 값.
   */
  private fun squarify(
    elements: List<TreemapElement>,
    row: List<TreemapElement>,
    w: Double,
  ) {
    if (elements.isNotEmpty()) {
      val remainPopped = ArrayDeque(elements)
      val c = remainPopped.removeFirst()
      val concatRow: MutableList<TreemapElement> = ArrayList(row)
      concatRow.add(c)
      val remaining: List<TreemapElement> = ArrayList(remainPopped)
      val worstConcat = worst(concatRow, w)
      val worstRow = worst(row, w)
      if (row.isEmpty() || worstRow > worstConcat || isEqual(worstRow, worstConcat)) {
        if (remaining.isEmpty()) {
          layoutRow(concatRow, w)
        } else {
          squarify(remaining, concatRow, w)
        }
      } else {
        layoutRow(row, w)
        squarify(elements, ArrayList(), minimumSide())
      }
    }
  }

  /**
   * @return 사용 가능한 영역에서 너비와 높이의 작은 값 반환
   */
  private fun minimumSide(): Double = min(heightLeft, widthLeft)

  /**
   * 2개의 Double 타입의 정확한 비교를 위한 함수
   */
  private fun isEqual(source: Double, target: Double): Boolean = abs(source - target) < 0.00001

  /**
   * 레이아웃 변경
   */
  private fun changeLayout() {
    layoutOrientation = layoutOrientation.toggle(layoutOrientation)
  }

  /**
   * - w값에 따른 최악의 형태(면적 비율)를 계산.
   * - 반환 값이 커질수록 w 값에서 더 최악의 형태.
   * - 반환 값이 작을수록 w 값에서 더 좋은 형태.
   * @return w 값과 요소들의 면적을 고려하여 최악의 값을 반환.
   */
  private fun worst(ch: List<TreemapElement>, w: Double): Double {
    if (ch.isEmpty()) {
      return Double.MAX_VALUE
    }
    var areaSum = 0.0
    var maxArea = 0.0
    var minArea = Double.MAX_VALUE
    for (item in ch) {
      val area = item.area
      areaSum += area
      minArea = min(minArea, area)
      maxArea = max(maxArea, area)
    }
    val sqw = w * w
    val sqAreaSum = areaSum * areaSum
    return max(
      sqw * maxArea / sqAreaSum,
      sqAreaSum / (sqw * minArea),
    )
  }

  /**
   * squarify로 인해 그릴 준비가 완료되면 노드들의 좌표를 구하는 함수
   */
  private fun layoutRow(row: List<TreemapElement>, w: Double) {
    var totalArea = 0.0
    for (element in row) {
      val area = element.area
      totalArea += area
    }
    if (layoutOrientation == LayoutOrientation.VERTICAL) {
      val rowWidth = totalArea / w
      var topItem = 0.0
      for (element in row) {
        val area = element.area
        val h = area / rowWidth
        element.top = top + topItem
        element.left = left
        element.width = rowWidth
        element.height = h
        topItem += h
      }
      widthLeft -= rowWidth
      // this.heightLeft -= w;
      left += rowWidth
      val minimumSide = minimumSide()
      if (!isEqual(minimumSide, heightLeft)) {
        changeLayout()
      }
    } else {
      val rowHeight = totalArea / w
      var rowLeft = 0.0
      for (item in row) {
        val area = item.area
        val wi = area / rowHeight
        item.top = top
        item.left = left + rowLeft
        item.height = rowHeight
        item.width = wi
        rowLeft += wi
      }
      // this.widthLeft -= rowHeight;
      heightLeft -= rowHeight
      top += rowHeight
      val minimumSide = minimumSide()
      if (!isEqual(minimumSide, widthLeft)) {
        changeLayout()
      }
    }
  }

  /**
   * 화면에 맞게 비중 값을 재 설정하는 함수
   * @param [elements] 비중이 담긴 차트 요소 리스트
   */
  private fun scaleArea(elements: List<TreemapElement>) {
    val areaGiven = width * height
    var areaTotal = 0.0
    for (element in elements) {
      val area = element.area
      areaTotal += area
    }
    val ratio = areaTotal / areaGiven
    for (element in elements) {
      val area = element.area / ratio
      element.area = area
    }
  }
}
