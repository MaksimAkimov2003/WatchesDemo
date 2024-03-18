package com.akimov.watchesview

import com.akimov.watchesview.models.HoursAndMinutesLists
import com.akimov.watchesview.models.Point
import com.akimov.watchesview.models.PointWithText
import com.akimov.watchesview.utils.circularShift
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Calculator {
    companion object {
        const val TOTAL_DEGREES = 360
        const val HOURS_ON_CLOCK = 12
        const val MINUTES_IN_HOUR = 60
        const val SECONDS_IN_MINUTE = 60
        const val HOUR_HAND_PERCENTAGE = 0.6f
        const val SECONDS_HAND_PERCENTAGE = 1f
        const val MINUTES_HAND_PERCENTAGE = 0.8f
        const val START_OFFSET = 0.2
        const val MAIN_TO_INNER_CIRCLE_COEFFICIENT = 0.8f
        const val INNER_TO_TEXT_CIRCLE_COEFFICIENT = MAIN_TO_INNER_CIRCLE_COEFFICIENT
    }

    fun calculateTextCoordinates(
        pointWithText: PointWithText,
        fontBottomCoordinate: Float,
        fontTopCoordinate: Float,
    ): PointWithText {
        val textHeight = fontBottomCoordinate - fontTopCoordinate
        val textY = pointWithText.pointTextY + textHeight / 4

        return pointWithText.copy(
            textX = pointWithText.pointTextX, textY = textY
        )
    }

    fun calculateTimePoints(
        pointsCount: Int,
        offset: Float,
        mainCircleCenterX: Float,
        mainCircleCenterY: Float,
        mainCircleRadius: Float,
        textOffset: Float,
    ): HoursAndMinutesLists {
        val angleStep = 2 * PI / pointsCount
        val hours = mutableListOf<PointWithText>()
        val minutes = mutableListOf<Point>()

        for (i in 0 until pointsCount) {
            val angle = i * angleStep
            val x = mainCircleCenterX + (mainCircleRadius * MAIN_TO_INNER_CIRCLE_COEFFICIENT) * cos(
                angle
            )
            val y = mainCircleCenterY + (mainCircleRadius * MAIN_TO_INNER_CIRCLE_COEFFICIENT) * sin(angle)

            if (i % 5 == 0) {
                val pointTextX =
                    mainCircleCenterX + (mainCircleRadius * MAIN_TO_INNER_CIRCLE_COEFFICIENT * INNER_TO_TEXT_CIRCLE_COEFFICIENT) * cos(
                        angle
                    )
                val pointTextY =
                    mainCircleCenterY + (mainCircleRadius * MAIN_TO_INNER_CIRCLE_COEFFICIENT * INNER_TO_TEXT_CIRCLE_COEFFICIENT) * sin(
                        angle
                    )
                hours += PointWithText(
                    x = x.toFloat(),
                    y = y.toFloat(),
                    pointTextX = pointTextX.toFloat(),
                    pointTextY = pointTextY.toFloat()
                )
            } else {
                minutes.add(Point(x.toFloat(), y.toFloat()))
            }
        }
        hours.circularShift(2)

        return HoursAndMinutesLists(hours, minutes)
    }

    fun calculateMainCircleRadius(
        w: Int, h: Int, mainCircleStrokeWidth: Float
    ) = (min(w, h) / 2f) - mainCircleStrokeWidth

    fun getCenterY(height: Int): Float = height / 2f

    fun getCenterX(width: Int) = width / 2f
    fun calculateHourHandCoordinates(hours: Int, minutes: Int, seconds: Int): FloatArray {
        val hourAngle =
            ((hours % HOURS_ON_CLOCK) * TOTAL_DEGREES / HOURS_ON_CLOCK) + (minutes * TOTAL_DEGREES / (HOURS_ON_CLOCK * MINUTES_IN_HOUR)).toFloat()
        return calculateHandCoordinates(hourAngle)
    }

    // Calculate the coordinates of the minute hand
    fun calculateMinuteHandCoordinates(minutes: Int, seconds: Int): FloatArray {
        val minuteAngle =
            (minutes * TOTAL_DEGREES / MINUTES_IN_HOUR) + (seconds * TOTAL_DEGREES / (MINUTES_IN_HOUR * SECONDS_IN_MINUTE)).toFloat()
        return calculateHandCoordinates(minuteAngle)
    }

    // Calculate the coordinates of the second hand
    fun calculateSecondHandCoordinates(seconds: Int): FloatArray {
        val secondAngle = seconds * TOTAL_DEGREES / SECONDS_IN_MINUTE
        return calculateHandCoordinates(secondAngle.toFloat())
    }

    fun getHourHandLength(circleRadius: Float): Float = circleRadius * HOUR_HAND_PERCENTAGE

    fun getSecondHandLength(circleRadius: Float) = circleRadius * SECONDS_HAND_PERCENTAGE

    fun getMinuteHandLength(circleRadius: Float) = circleRadius * MINUTES_HAND_PERCENTAGE

    fun getHourStartY(
        hourCoords: FloatArray,
        hourHandLength: Float,
        mainCircleCenterY: Float
    ) =
        mainCircleCenterY - hourCoords[1] * (hourHandLength * START_OFFSET)

    fun getHourStartX(
        hourCoords: FloatArray,
        hourHandLength: Float,
        mainCircleCenterX: Float
    ) =
        mainCircleCenterX - hourCoords[0] * (hourHandLength * START_OFFSET)

    // Helper function to calculate hand coordinates based on angle
    private fun calculateHandCoordinates(angle: Float): FloatArray {
        val radians =
            Math.toRadians((angle - 90).toDouble()) // Convert angle to radians and offset by 90 degrees to start from 12 o'clock position
        val x = cos(radians).toFloat()
        val y = sin(radians).toFloat()
        return floatArrayOf(x, y)
    }

    fun getMinuteStartY(
        minuteCoords: FloatArray,
        minuteHandLength: Float,
        mainCircleCenterY: Float
    ) =
        mainCircleCenterY - minuteCoords[1] * (minuteHandLength * START_OFFSET)

    fun getMinuteStartX(
        minuteCoords: FloatArray,
        minuteHandLength: Float,
        mainCircleCenterX: Float
    ) =
        mainCircleCenterX - minuteCoords[0] * (minuteHandLength * START_OFFSET)

    fun getMinuteEndY(
        minuteCoords: FloatArray,
        minuteHandLength: Float,
        mainCircleCenterY: Float
    ) =
        mainCircleCenterY + minuteCoords[1] * minuteHandLength

    fun getMinuteEndX(
        minuteCoords: FloatArray,
        minuteHandLength: Float,
        mainCircleCenterX: Float
    ) =
        mainCircleCenterX + minuteCoords[0] * minuteHandLength

    fun getSecondEndY(
        secondCoords: FloatArray,
        secondHandLength: Float,
        mainCircleCenterY: Float
    ) =
        mainCircleCenterY + secondCoords[1] * secondHandLength

    fun getHourEndY(
        hourCoords: FloatArray,
        hourHandLength: Float,
        mainCircleCenterY: Float
    ) =
        mainCircleCenterY + hourCoords[1] * hourHandLength

    fun getSecondEndX(
        secondCoords: FloatArray,
        secondHandLength: Float,
        mainCircleCenterX: Float
    ) =
        mainCircleCenterX + secondCoords[0] * secondHandLength

    fun getHourEndX(
        hourCoords: FloatArray,
        hourHandLength: Float,
        mainCircleCenterX: Float
    ) =
        mainCircleCenterX + hourCoords[0] * hourHandLength

    fun getSecondStartY(
        secondCoords: FloatArray,
        secondHandLength: Float,
        mainCircleCenterY: Float
    ) =
        mainCircleCenterY - secondCoords[1] * (secondHandLength * START_OFFSET)

    fun getSecondStartX(
        secondCoords: FloatArray,
        secondHandLength: Float,
        mainCircleCenterX: Float
    ) =
        mainCircleCenterX - secondCoords[0] * (secondHandLength * START_OFFSET)
}
