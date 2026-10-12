package com.eenact.dailymetrics.domain

import com.eenact.dailymetrics.domain.model.EntryType
import com.eenact.dailymetrics.domain.model.MeasureUnit
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.domain.model.TaskRecord
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

val UTC = TimeZone.UTC

val yesNoTask = Task(id = 1, name = "Exercise", type = EntryType.YesNo, unit = MeasureUnit.Other, color = 0xFF2CA02C.toInt())
val numberTask = Task(id = 2, name = "Sleep", type = EntryType.Number, unit = MeasureUnit.Hour, color = 0xFFD62728.toInt())

fun millis(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
    LocalDateTime(year, month, day, hour, minute).toInstant(UTC).toEpochMilliseconds()

fun record(task: Task, time: Long, value: String, comment: String = "") = TaskRecord(task, time, value, comment)
