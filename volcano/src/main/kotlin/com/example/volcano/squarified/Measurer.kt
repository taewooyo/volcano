package com.example.volcano.squarified

interface Measurer {

    fun measureNodes(values: List<Double>, width: Int, height: Int): List<TreemapNode>
}