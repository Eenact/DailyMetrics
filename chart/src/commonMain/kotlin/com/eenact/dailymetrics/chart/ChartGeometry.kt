package com.eenact.dailymetrics.chart

import kotlin.math.roundToInt

/** Values of the horizontal grid lines: 0, yStep, 2·yStep … up to and including [yMax]. */
internal fun yTicks(yMax: Float, yStep: Float): List<Float> {
    if (yStep <= 0f || yStep.isNaN()) return listOf(0f)
    return generateSequence(0f) { it + yStep }.takeWhile { it <= yMax + 0.001f }.toList()
}

/** Whole numbers without a decimal point, everything else rounded to one decimal. */
internal fun formatTick(value: Float): String {
    val rounded = value.roundToInt()
    return if (rounded.toFloat() == value) rounded.toString() else ((value * 10).roundToInt() / 10f).toString()
}

/** Maps chart x/y values into the pixel rectangle the series are drawn in. */
internal class PlotArea(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    private val xMin: Long,
    xMax: Long,
    private val yMax: Float,
) {
    private val xSpan = (xMax - xMin).coerceAtLeast(1L).toFloat()

    fun xOf(x: Long): Float = left + (x - xMin) / xSpan * (right - left)

    fun yOf(y: Float): Float = if (yMax > 0f) bottom - y / yMax * (bottom - top) else bottom
}

/** Left edge of an x-axis label centred on [x], kept inside the canvas where it fits. */
internal fun labelLeft(x: Float, labelWidth: Float, canvasWidth: Float): Float =
    (x - labelWidth / 2f).coerceAtMost(canvasWidth - labelWidth).coerceAtLeast(0f)
