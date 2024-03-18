package com.akimov.watchesview.models

data class PointWithText(
    val x: Float,
    val y: Float,
    val textX: Float = 0f,
    val textY: Float = 0f,
    val pointTextX: Float = 0f,
    val pointTextY: Float = 0f,
)

data class Point(
    val x: Float,
    val y: Float
)
