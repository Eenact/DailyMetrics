package com.eenact.dailymetrics.chart

data class ChartPoint(val x: Long, val y: Float)

data class ChartSeries(
    val taskId: Long,
    val name: String,
    /** ARGB color of the line, dots and legend entry. */
    val color: Int,
    val points: List<ChartPoint>,
)

data class AxisLabel(val position: Long, val text: String)

/** Everything [LineChart] draws: series in x/y space plus the axis ranges and labels. */
data class ChartData(
    val series: List<ChartSeries>,
    val xMin: Long,
    val xMax: Long,
    val xLabels: List<AxisLabel>,
    val yMax: Float,
    val yStep: Float,
) {
    val isEmpty: Boolean get() = series.isEmpty()
}
