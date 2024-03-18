package com.akimov.watchesview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.akimov.watchesview.models.Point
import com.akimov.watchesview.models.PointWithText
import com.akimov.watchesview.utils.dpToPx
import com.akimov.watchesview.utils.pxToDp
import java.time.LocalDateTime
import kotlin.math.min

class WatchesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(
    context, attrs, defStyleAttr, defStyleRes
) {
    private companion object {
        const val MAIN_CIRCLE_STROKE_DP = 8
        const val MINUTES_COUNT = 60
        const val POINTS_COUNT = MINUTES_COUNT
        const val MINUTES_HAND_WIDTH_DP = 4
        const val BIG_DOT_SIZE_DP = MINUTES_HAND_WIDTH_DP
        const val TEXT_SIZE_DP = 32
        const val TEXT_OFFSET_DP = TEXT_SIZE_DP
        const val OFFSET_BETWEEN_MAIN_AND_INNER_CIRCLE_DP = TEXT_SIZE_DP
        const val SMALL_DOT_SIZE_DP = 1
        const val HOURS_COUNT = 12
        const val HOURS_HAND_WIDTH_DP = 8
        const val SECOND_HAND_WIDTH_DP = 2
        const val DELAY_UPDATE_TIME = 100L
        const val MIN_VIEW_HEIGHT_DP = 300
    }

    private val secondHandPaintEnd = Paint().apply {
        this.color = Color.BLACK
        this.strokeWidth = (SECOND_HAND_WIDTH_DP / 2).dpToPx(context)
        this.isAntiAlias = true
    }
    private val calculator = Calculator()

    private val minuteHandPaint = Paint().apply {
        this.color = Color.BLACK
        this.strokeWidth = MINUTES_HAND_WIDTH_DP.dpToPx(context)
        this.strokeCap = Paint.Cap.ROUND
        this.isAntiAlias = true
    }

    private val hourHandPaint = Paint().apply {
        this.color = Color.BLACK
        this.strokeWidth = HOURS_HAND_WIDTH_DP.dpToPx(context)
        this.strokeCap = Paint.Cap.ROUND
        this.isAntiAlias = true
    }

    private val textPaint: Paint = Paint().apply {
        this.textSize = TEXT_SIZE_DP.dpToPx(context)
        this.color = Color.BLACK
        this.textAlign = Paint.Align.CENTER
        this.isAntiAlias = true
    }
    private val textOffset = TEXT_OFFSET_DP.dpToPx(context)
    private val bigDotPaint = Paint().apply {
        this.color = Color.BLACK
        this.style = Paint.Style.FILL
        this.isAntiAlias = true
    }
    private var bigDotRadius = BIG_DOT_SIZE_DP.dpToPx(context)
    private var smallDotRadius = SMALL_DOT_SIZE_DP.dpToPx(context)
    private var mainCircleCenterX = 0f
    private var mainCircleCenterY = 0f
    private var mainCircleRadius = 0f
    private var innerCircleRadius = 0f
    private var textCircleRadius = 0f
    private var mainCircleStrokeWidth = MAIN_CIRCLE_STROKE_DP.dpToPx(context)
    private val mainCirclePaint = Paint().apply {
        this.color = Color.BLACK
        this.style = Paint.Style.STROKE
        this.strokeWidth = mainCircleStrokeWidth
        this.isAntiAlias = true
    }
    private var hourPointWithTexts: Array<PointWithText> =
        Array(HOURS_COUNT) { PointWithText(0f, 0f) }
    private var minutePoints: Array<Point> = Array(MINUTES_COUNT) { Point(0f, 0f) }

    private var localDateTime = LocalDateTime.now()

    private val runnable = object : Runnable {
        override fun run() {
            localDateTime = LocalDateTime.now()
            invalidate()
            postDelayed(this, DELAY_UPDATE_TIME)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        runnable.run()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(runnable)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val requestedWidth = MeasureSpec.getSize(
            widthMeasureSpec
        )

        val requestedHeight = MeasureSpec.getSize(
            heightMeasureSpec
        )

        val size = min(requestedWidth, requestedHeight)

        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setCircleParams(w, h)
        setPointsParams()
        setTextSize(width = w, height = h)
        initParams(width = w, height = h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        drawMainCircle(canvas)
        drawHours(canvas)
        drawMinutes(canvas)

        with(localDateTime) {
            drawHourHand(
                hours = hour,
                minutes = minute,
                seconds = second,
                canvas = canvas
            )
            drawMinuteHand(
                minutes = minute,
                seconds = second,
                canvas = canvas
            )
            drawSecondHand(
                seconds = second,
                canvas = canvas
            )
        }
    }

    private fun initParams(width: Int, height: Int) {
        if (min(width, height).pxToDp(context) < MIN_VIEW_HEIGHT_DP) {
            val coefficient = 2
            mainCirclePaint.strokeWidth = mainCirclePaint.strokeWidth / coefficient
            bigDotRadius /= coefficient
            smallDotRadius /= coefficient
            hourHandPaint.strokeWidth /= coefficient
            minuteHandPaint.strokeWidth /= coefficient
            secondHandPaintEnd.strokeWidth /= coefficient
        }
    }

    private fun setTextSize(width: Int, height: Int) {
        textPaint.textSize = when (min(width, height).pxToDp(context)) {
            in 0..100 -> 0.dpToPx(context)
            in 101..200 -> 8.dpToPx(context)
            in 201..MIN_VIEW_HEIGHT_DP -> 16.dpToPx(context)
            in 301..600 -> 32.dpToPx(context)
            else -> 32.dpToPx(context)
        }
    }

    private fun drawSecondHand(seconds: Int, canvas: Canvas) {
        // Calculate second hand coordinates
        val secondCoords = calculator.calculateSecondHandCoordinates(seconds)
        val secondHandLength =
            calculator.getSecondHandLength(circleRadius = innerCircleRadius)// Adjust as needed
        val secondEndX = calculator.getSecondEndX(
            secondCoords = secondCoords,
            secondHandLength = secondHandLength,
            mainCircleCenterX = mainCircleCenterX
        )
        val secondEndY = calculator.getSecondEndY(
            secondCoords = secondCoords,
            secondHandLength = secondHandLength,
            mainCircleCenterY = mainCircleCenterY
        )

        val secondStartX = calculator.getSecondStartX(
            secondCoords = secondCoords,
            secondHandLength = secondHandLength,
            mainCircleCenterX = mainCircleCenterX
        )
        val secondStartY = calculator.getSecondStartY(
            secondCoords = secondCoords,
            secondHandLength = secondHandLength,
            mainCircleCenterY = mainCircleCenterY
        )

        // Draw the second hand
        canvas.drawLine(
            secondStartX.toFloat(),
            secondStartY.toFloat(),
            secondEndX,
            secondEndY,
            secondHandPaintEnd
        )
    }

    private fun drawMinuteHand(minutes: Int, seconds: Int, canvas: Canvas) {
        // Calculate minute hand coordinates
        val minuteCoords = calculator.calculateMinuteHandCoordinates(
            minutes,
            seconds
        )

        // Define lengths of minute and second hands
        val minuteHandLength = calculator.getMinuteHandLength(circleRadius = textCircleRadius)

        // Calculate endpoints of minute and second hands
        val minuteEndX = calculator.getMinuteEndX(
            minuteCoords = minuteCoords,
            minuteHandLength = minuteHandLength,
            mainCircleCenterX = mainCircleCenterX
        )
        val minuteEndY = calculator.getMinuteEndY(
            minuteCoords = minuteCoords,
            minuteHandLength = minuteHandLength,
            mainCircleCenterY = mainCircleCenterY
        )

        val minuteStartX = calculator.getMinuteStartX(
            minuteCoords = minuteCoords,
            minuteHandLength = minuteHandLength,
            mainCircleCenterX = mainCircleCenterX
        )
        val minuteStartY = calculator.getMinuteStartY(
            minuteCoords = minuteCoords,
            minuteHandLength = minuteHandLength,
            mainCircleCenterY = mainCircleCenterY
        )

        // Draw the minute hand
        canvas.drawLine(
            minuteStartX.toFloat(),
            minuteStartY.toFloat(),
            minuteEndX,
            minuteEndY,
            minuteHandPaint
        )
    }

    private fun drawHourHand(
        hours: Int,
        minutes: Int,
        seconds: Int,
        canvas: Canvas
    ) {
        val hourCoords = calculator.calculateHourHandCoordinates(
            hours,
            minutes,
            seconds
        )

        // Calculate the length of the hour hand
        val hourHandLength = calculator.getHourHandLength(
            circleRadius = textCircleRadius
        )

        val hourStartX = calculator.getHourStartX(
            hourCoords = hourCoords,
            hourHandLength = hourHandLength,
            mainCircleCenterX = mainCircleCenterX
        )
        val hourStartY = calculator.getHourStartY(
            hourCoords = hourCoords,
            hourHandLength = hourHandLength,
            mainCircleCenterY = mainCircleCenterY
        )

        // Calculate the endpoint of the hour hand
        val hourEndX = calculator.getHourEndX(
            hourCoords = hourCoords,
            hourHandLength = hourHandLength,
            mainCircleCenterX = mainCircleCenterX
        )
        val hourEndY = calculator.getHourEndY(
            hourCoords = hourCoords,
            hourHandLength = hourHandLength,
            mainCircleCenterY = mainCircleCenterY
        )

        canvas.drawLine(
            hourStartX.toFloat(),
            hourStartY.toFloat(),
            hourEndX,
            hourEndY,
            hourHandPaint
        )
    }

    private fun setPointsParams() {
        val newHoursAndMinutes = calculator.calculateTimePoints(
            mainCircleCenterX = mainCircleCenterX,
            mainCircleCenterY = mainCircleCenterY,
            mainCircleRadius = mainCircleRadius,
            pointsCount = POINTS_COUNT,
            textOffset = textOffset,
            offset = OFFSET_BETWEEN_MAIN_AND_INNER_CIRCLE_DP.dpToPx(context)
        )

        hourPointWithTexts = newHoursAndMinutes.hours.toMutableList().toTypedArray()
        minutePoints = newHoursAndMinutes.minutes.toMutableList().toTypedArray()

        hourPointWithTexts = hourPointWithTexts.map { pointWithText ->
            calculator.calculateTextCoordinates(
                pointWithText = pointWithText,
                fontBottomCoordinate = textPaint.fontMetrics.bottom,
                fontTopCoordinate = textPaint.fontMetrics.top
            )
        }.toMutableList().toTypedArray()
    }

    private fun setCircleParams(w: Int, h: Int) {
        mainCircleCenterX = calculator.getCenterX(width = w)
        mainCircleCenterY = calculator.getCenterY(height = h)
        mainCircleRadius = calculator.calculateMainCircleRadius(
            w = w,
            h = h,
            mainCircleStrokeWidth = mainCircleStrokeWidth
        )
        innerCircleRadius =
            mainCircleRadius * Calculator.MAIN_TO_INNER_CIRCLE_COEFFICIENT

        textCircleRadius = innerCircleRadius * Calculator.INNER_TO_TEXT_CIRCLE_COEFFICIENT
    }

    private fun drawMinutes(canvas: Canvas) {
        for (dot in minutePoints) {
            canvas.drawCircle(
                dot.x, dot.y, smallDotRadius, bigDotPaint
            )
        }
    }

    private fun drawHours(canvas: Canvas) {
        hourPointWithTexts.forEachIndexed { index, dot ->
            canvas.drawCircle(
                dot.x, dot.y, bigDotRadius, bigDotPaint
            )
            canvas.drawText(
                "${index + 1}", dot.textX, dot.textY, textPaint
            )
        }
    }

    private fun drawMainCircle(canvas: Canvas) {
        canvas.drawCircle(
            mainCircleCenterX, mainCircleCenterY, mainCircleRadius, mainCirclePaint
        )
    }

}