package com.eenact.dailymetrics.chart

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class ChartDataTest : FunSpec({

    fun data(series: List<ChartSeries>) =
        ChartData(series = series, xMin = 0L, xMax = 10L, xLabels = emptyList(), yMax = 10f, yStep = 2f)

    test("chart without series is empty") {
        data(emptyList()).isEmpty.shouldBeTrue()
    }

    test("chart with a series is not empty even without points") {
        val series = ChartSeries(taskId = 1L, name = "Water", color = 0xFF2196F3.toInt(), points = emptyList())
        data(listOf(series)).isEmpty.shouldBeFalse()
    }
})
