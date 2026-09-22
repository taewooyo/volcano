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
package com.taewooyo.volcano.squarified

import com.taewooyo.volcano.configuration.LayoutOrientation
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
   * Functions to measure and set the size of elements in a chart.
   * @param [values] specific gravity value
   * @param [width] horizontal length of the chart
   * @param [height] vertical length of the chart
   * @return Returns a list of horizontal, vertical, and offset nodes
   */
  override fun measureNodes(values: List<Double>, width: Int, height: Int): List<TreemapNode> {
    setSizeAndValues(values, width.toDouble(), height.toDouble())
    return measureNodes()
  }

  /**
   * Function to set the size and proportion of the chart
   * @param [values] specific gravity value
   * @param [width] horizontal length of the chart
   * @param [height] vertical length of the chart
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
   * Function that returns the coordinates and sizes of the nodes in the chart
   * @return Returns a list of chart nodes with coordinates and sizes set through the chart map placement algorithm
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
   * @param [elements] specific gravity value
   * @param [row] current row
   * @param [w] The remaining value of the drawable area
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
   * @return Return small values of width and height from available area
   */
  private fun minimumSide(): Double = min(heightLeft, widthLeft)

  /**
   * Function for accurate comparison of two Double types
   */
  private fun isEqual(source: Double, target: Double): Boolean = abs(source - target) < 0.00001

  /**
   * Change layout
   */
  private fun changeLayout() {
    layoutOrientation = layoutOrientation.toggle(layoutOrientation)
  }

  /**
   * - Calculate the worst shape (area ratio) according to the w value.
   * - The larger the return value, the worse the worst form for the w value.
   * - The smaller the return value, the better the shape for the w value
   * @return Returns the worst value considering the w value and the area of ​​the elements
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
   * A function that calculates the coordinates of nodes when preparation for drawing is completed with squarify
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
   * Function to reset the specific gravity value to fit the screen
   * @param [elements] List of chart elements with proportions
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
