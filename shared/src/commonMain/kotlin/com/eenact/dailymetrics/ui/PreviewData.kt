package com.eenact.dailymetrics.ui

import com.eenact.dailymetrics.domain.ChartDataBuilder
import com.eenact.dailymetrics.domain.DashboardSnapshot
import com.eenact.dailymetrics.domain.model.EntryType
import com.eenact.dailymetrics.domain.model.MeasureUnit
import com.eenact.dailymetrics.domain.model.Reminder
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.domain.model.TaskRecord
import com.eenact.dailymetrics.domain.model.TimeRange
import com.eenact.dailymetrics.ui.theme.LegendColors
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/** Fixed sample data for @Preview composables. Not used by the app at runtime. */
internal object PreviewData {
    private const val HOUR = 3_600_000L

    val now: Instant = Instant.fromEpochMilliseconds(1_790_000_000_000L)
    val zone: TimeZone = TimeZone.UTC

    val water = Task(id = 1, name = "Water", type = EntryType.Number, unit = MeasureUnit.Other, color = LegendColors[0])
    val workout = Task(
        id = 2,
        name = "Workout",
        type = EntryType.Number,
        unit = MeasureUnit.Min,
        color = LegendColors[2],
        reminder = Reminder(LocalTime(18, 30), setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
    )
    val meditated = Task(id = 3, name = "Meditated", type = EntryType.YesNo, unit = MeasureUnit.Other, color = LegendColors[3])

    val tasks = listOf(water, workout, meditated)

    val records: List<TaskRecord> = listOf(
        TaskRecord(water, now.toEpochMilliseconds() - 2 * HOUR, "6.0", ""),
        TaskRecord(workout, now.toEpochMilliseconds() - 5 * HOUR, "45.0", "Evening run"),
        TaskRecord(meditated, now.toEpochMilliseconds() - 9 * HOUR, "1", ""),
        TaskRecord(water, now.toEpochMilliseconds() - 26 * HOUR, "8.0", ""),
        TaskRecord(workout, now.toEpochMilliseconds() - 29 * HOUR, "30.0", ""),
        TaskRecord(water, now.toEpochMilliseconds() - 50 * HOUR, "5.0", "Hot day"),
        TaskRecord(meditated, now.toEpochMilliseconds() - 52 * HOUR, "0", ""),
    )

    fun snapshot(range: TimeRange = TimeRange.Week): DashboardSnapshot =
        ChartDataBuilder.build(records, tasks.map { it.id }.toSet(), range, now, zone)
}
