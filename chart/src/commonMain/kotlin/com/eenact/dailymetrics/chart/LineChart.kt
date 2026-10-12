package com.eenact.dailymetrics.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Default color of the "no data" message (MPAndroidChart's no-data color in the original app). */
val ChartNoDataText = Color(0xFFF7BD33)

/**
 * Multi-series line chart drawn with Compose Canvas, mirroring the MPAndroidChart setup of the
 * original app: bottom date labels, left value axis, grid, dots on every point and a legend.
 */
@Composable
fun LineChart(
    data: ChartData,
    noDataText: String,
    modifier: Modifier = Modifier,
    noDataColor: Color = ChartNoDataText,
) {
    if (data.isEmpty) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(noDataText, color = noDataColor, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

    // Replays the left-to-right reveal animation whenever the data changes.
    val reveal = remember(data) { Animatable(0f) }
    LaunchedEffect(data) { reveal.animateTo(1f, tween(durationMillis = 500, easing = LinearEasing)) }

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 4.dp, top = 12.dp, end = 16.dp),
        ) {
            val yTicks = yTicks(data.yMax, data.yStep)
            val yLabelWidth = yTicks.maxOf { textMeasurer.measure(formatTick(it), labelStyle).size.width }.toFloat()
            val xLabelHeight = textMeasurer.measure("0", labelStyle).size.height.toFloat()

            val left = yLabelWidth + 8.dp.toPx()
            val right = size.width
            val top = 0f
            val bottom = size.height - xLabelHeight - 6.dp.toPx()
            val plot = PlotArea(left, top, right, bottom, data.xMin, data.xMax, data.yMax)

            fun xOf(x: Long) = plot.xOf(x)
            fun yOf(y: Float) = plot.yOf(y)

            // Horizontal grid + value labels.
            yTicks.forEach { tick ->
                val y = yOf(tick)
                drawLine(gridColor, Offset(left, y), Offset(right, y), strokeWidth = 1f)
                val layout = textMeasurer.measure(formatTick(tick), labelStyle)
                drawText(layout, topLeft = Offset(left - layout.size.width - 6.dp.toPx(), y - layout.size.height / 2f))
            }

            // Vertical grid + time labels.
            data.xLabels.forEach { label ->
                val x = xOf(label.position)
                drawLine(gridColor, Offset(x, top), Offset(x, bottom), strokeWidth = 1f)
                val layout = textMeasurer.measure(label.text, labelStyle)
                val textX = labelLeft(x, layout.size.width.toFloat(), size.width)
                drawText(layout, topLeft = Offset(textX, bottom + 4.dp.toPx()))
            }

            // Series, revealed from left to right.
            clipRect(left = 0f, top = top - 8.dp.toPx(), right = left + (right - left) * reveal.value + 8.dp.toPx(), bottom = bottom + 8.dp.toPx()) {
                data.series.forEach { series ->
                    val color = Color(series.color)
                    val points = series.points.map { Offset(xOf(it.x), yOf(it.y)) }
                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, color, style = Stroke(width = 1.5.dp.toPx()))
                    }
                    points.forEach { drawCircle(color, radius = 4.dp.toPx(), center = it) }
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            data.series.forEach { series ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).background(Color(series.color)))
                    Spacer(Modifier.width(4.dp))
                    Text(series.name, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp))
                }
            }
        }
    }
}
