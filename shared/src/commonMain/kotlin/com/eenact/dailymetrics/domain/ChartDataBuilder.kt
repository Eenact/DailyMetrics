package com.eenact.dailymetrics.domain

import com.eenact.dailymetrics.chart.AxisLabel
import com.eenact.dailymetrics.chart.ChartData
import com.eenact.dailymetrics.chart.ChartPoint
import com.eenact.dailymetrics.chart.ChartSeries
import com.eenact.dailymetrics.domain.model.TaskRecord
import com.eenact.dailymetrics.domain.model.TimeRange
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Instant

/** Dashboard content for one time range: the chart and the records listed below it. */
data class DashboardSnapshot(
    val chart: ChartData,
    /** Records in range, newest first. */
    val records: List<TaskRecord>,
)

object ChartDataBuilder {

    /**
     * Builds the dashboard content for [range] ending at [now].
     *
     * [visibleTaskIds] holds the tasks enabled in the filter; null means all tasks.
     */
    fun build(
        records: List<TaskRecord>,
        visibleTaskIds: Set<Long>?,
        range: TimeRange,
        now: Instant,
        zone: TimeZone,
    ): DashboardSnapshot {
        val visible = records.filter { visibleTaskIds == null || it.task.id in visibleTaskIds }
        val start = rangeStart(range, now, zone, earliestRecord = visible.minOfOrNull { it.time })
        val inRange = visible.filter { it.time >= start }

        val series = inRange
            .groupBy { it.task.id }
            .entries
            .sortedBy { it.key }
            .map { (taskId, taskRecords) ->
                val task = taskRecords.first().task
                ChartSeries(
                    taskId = taskId,
                    name = task.name,
                    color = task.color,
                    points = taskRecords.sortedBy { it.time }.map { ChartPoint(it.time, it.numericValue) },
                )
            }

        val (xMax, labels) = xAxis(range, start, now, zone)
        val maxValue = inRange.maxOfOrNull { it.numericValue } ?: 0f
        val yStep = niceStep((maxValue + 1f) / 8f)
        val yMax = max(yStep, ceil((maxValue + 1f) / yStep) * yStep)

        return DashboardSnapshot(
            chart = ChartData(
                series = series,
                xMin = start,
                xMax = xMax,
                xLabels = labels.filter { it.position in start..xMax },
                yMax = yMax,
                yStep = yStep,
            ),
            records = inRange.sortedByDescending { it.time },
        )
    }

    /**
     * First timestamp (inclusive) shown for [range].
     *
     * [TimeRange.Max] starts at midnight of the day of [earliestRecord] (today when there is none).
     */
    fun rangeStart(range: TimeRange, now: Instant, zone: TimeZone, earliestRecord: Long? = null): Long {
        val today = now.toLocalDateTime(zone).date
        return when (range) {
            TimeRange.Day -> today.atStartOfDayIn(zone).toEpochMilliseconds()
            TimeRange.Max -> {
                val firstDay = earliestRecord?.let { minOf(it.toLocalDateTime(zone).date, today) } ?: today
                firstDay.atStartOfDayIn(zone).toEpochMilliseconds()
            }
            else -> now.minus(range.days, DateTimeUnit.DAY, zone).toEpochMilliseconds()
        }
    }

    private fun xAxis(range: TimeRange, start: Long, now: Instant, zone: TimeZone): Pair<Long, List<AxisLabel>> {
        val today = now.toLocalDateTime(zone).date
        val tomorrow = today.plus(DatePeriod(days = 1))

        if (range == TimeRange.Day) {
            val labels = (0 until 24 step 2).map { hour ->
                AxisLabel(today.atTime(LocalTime(hour, 0)).toInstant(zone).toEpochMilliseconds(), "${hour}h")
            }
            return tomorrow.atStartOfDayIn(zone).toEpochMilliseconds() to labels
        }

        val (stepDays, count) = when (range) {
            TimeRange.Week -> 2 to 4
            TimeRange.Month -> 7 to 5
            TimeRange.SixMonths -> 36 to 5
            // Five labels spread over the whole history, at least one day apart.
            TimeRange.Max -> {
                val span = start.toLocalDateTime(zone).date.daysUntil(tomorrow)
                max(1, (span + 3) / 4) to 5
            }
            TimeRange.Day -> error("handled above")
        }
        val labels = (0 until count).map { i ->
            val date = tomorrow.minus(DatePeriod(days = i * stepDays))
            AxisLabel(date.atStartOfDayIn(zone).toEpochMilliseconds(), "${date.day}/${date.month.ordinal + 1}")
        }.reversed()
        return tomorrow.atStartOfDayIn(zone).toEpochMilliseconds() to labels
    }

    /** Rounds [raw] up to 1, 2 or 5 times a power of ten (minimum 1). */
    internal fun niceStep(raw: Float): Float {
        if (raw <= 1f) return 1f
        var magnitude = 10f.pow(kotlin.math.floor(kotlin.math.log10(raw)))
        val normalized = raw / magnitude
        val nice = when {
            normalized <= 1f -> 1f
            normalized <= 2f -> 2f
            normalized <= 5f -> 5f
            else -> {
                magnitude *= 10f
                1f
            }
        }
        return nice * magnitude
    }
}
