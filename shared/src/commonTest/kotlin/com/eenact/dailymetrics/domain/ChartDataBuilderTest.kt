package com.eenact.dailymetrics.domain

import com.eenact.dailymetrics.domain.model.TimeRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class ChartDataBuilderTest {

    private val now = Instant.fromEpochMilliseconds(millis(2026, 9, 28, 21, 49))

    @Test
    fun weekRangeStartsSevenDaysAgoAtTheSameTime() {
        assertEquals(millis(2026, 9, 21, 21, 49), ChartDataBuilder.rangeStart(TimeRange.Week, now, UTC))
    }

    @Test
    fun dayRangeStartsAtMidnight() {
        assertEquals(millis(2026, 9, 28), ChartDataBuilder.rangeStart(TimeRange.Day, now, UTC))
    }

    @Test
    fun weekAxisHasALabelEveryTwoDays() {
        val chart = ChartDataBuilder.build(emptyList(), null, TimeRange.Week, now, UTC).chart
        assertEquals(listOf("23/9", "25/9", "27/9", "29/9"), chart.xLabels.map { it.text })
        assertEquals(millis(2026, 9, 29), chart.xMax)
    }

    @Test
    fun sixMonthRangeMatchesTheOriginalLabels() {
        assertEquals(millis(2026, 4, 1, 21, 49), ChartDataBuilder.rangeStart(TimeRange.SixMonths, now, UTC))
        val chart = ChartDataBuilder.build(emptyList(), null, TimeRange.SixMonths, now, UTC).chart
        assertEquals(listOf("8/5", "13/6", "19/7", "24/8", "29/9"), chart.xLabels.map { it.text })
    }

    @Test
    fun maxRangeStartsOnTheDayOfTheFirstVisibleRecord() {
        val records = listOf(
            record(yesNoTask, millis(2025, 1, 15, 10), "1"),
            record(numberTask, millis(2026, 9, 27, 8), "7.5"),
        )

        val all = ChartDataBuilder.build(records, null, TimeRange.Max, now, UTC)
        assertEquals(millis(2025, 1, 15), all.chart.xMin)
        assertEquals(2, all.records.size)
        assertEquals("29/9", all.chart.xLabels.last().text)
        assertTrue(all.chart.xLabels.all { it.position >= all.chart.xMin })

        // Hidden tasks do not stretch the range.
        val filtered = ChartDataBuilder.build(records, setOf(2L), TimeRange.Max, now, UTC)
        assertEquals(millis(2026, 9, 27), filtered.chart.xMin)
        assertEquals(listOf("27/9", "28/9", "29/9"), filtered.chart.xLabels.map { it.text })
    }

    @Test
    fun maxRangeWithoutRecordsStartsToday() {
        assertEquals(millis(2026, 9, 28), ChartDataBuilder.rangeStart(TimeRange.Max, now, UTC))
    }

    @Test
    fun dayAxisHasTwoHourLabels() {
        val chart = ChartDataBuilder.build(emptyList(), null, TimeRange.Day, now, UTC).chart
        assertEquals(12, chart.xLabels.size)
        assertEquals("0h", chart.xLabels.first().text)
        assertEquals("22h", chart.xLabels.last().text)
    }

    @Test
    fun keepsOnlyVisibleTasksInsideTheRange() {
        val records = listOf(
            record(yesNoTask, millis(2026, 9, 28, 8), "1"),
            record(yesNoTask, millis(2026, 9, 20, 8), "1"), // too old
            record(numberTask, millis(2026, 9, 27, 8), "7.5"),
        )

        val all = ChartDataBuilder.build(records, null, TimeRange.Week, now, UTC)
        assertEquals(2, all.records.size)
        assertEquals(listOf(1L, 2L), all.chart.series.map { it.taskId })
        assertTrue(all.records.first().time > all.records.last().time, "records are newest first")

        val filtered = ChartDataBuilder.build(records, setOf(2L), TimeRange.Week, now, UTC)
        assertEquals(listOf("Sleep"), filtered.records.map { it.task.name })
        assertEquals(listOf(2L), filtered.chart.series.map { it.taskId })
    }

    @Test
    fun valueAxisLeavesHeadroomAboveTheLargestValue() {
        val records = listOf(record(numberTask, millis(2026, 9, 28, 8), "7.5"))
        val chart = ChartDataBuilder.build(records, null, TimeRange.Week, now, UTC).chart
        assertTrue(chart.yMax >= 8.5f)
        assertEquals(0f, chart.yMax % chart.yStep)
    }

    @Test
    fun yesNoChartGoesUpToTwo() {
        val records = listOf(record(yesNoTask, millis(2026, 9, 28, 8), "1"))
        val chart = ChartDataBuilder.build(records, null, TimeRange.Week, now, UTC).chart
        assertEquals(2f, chart.yMax)
        assertEquals(1f, chart.yStep)
    }

    @Test
    fun niceStepRoundsToOneTwoOrFive() {
        assertEquals(1f, ChartDataBuilder.niceStep(0.3f))
        assertEquals(2f, ChartDataBuilder.niceStep(1.4f))
        assertEquals(5f, ChartDataBuilder.niceStep(3f))
        assertEquals(10f, ChartDataBuilder.niceStep(7f))
        assertEquals(50f, ChartDataBuilder.niceStep(31f))
    }
}
