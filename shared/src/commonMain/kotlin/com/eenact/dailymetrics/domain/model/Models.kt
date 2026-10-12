package com.eenact.dailymetrics.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

/**
 * Kind of value a task accepts.
 *
 * The constant names are persisted in the database (Room stores enums by name), so they must never
 * be renamed. Use [label] for anything shown to the user.
 */
enum class EntryType(val label: String) {
    YesNo("Yes/No"),
    Number("Number");

    override fun toString(): String = label
}

/**
 * Unit used to measure a [EntryType.Number] task.
 *
 * The constant names are persisted in the database, so they must never be renamed.
 */
enum class MeasureUnit(val label: String) {
    Gram("Grams"),
    Sec("Seconds"),
    Min("Minutes"),
    Hour("Hours"),
    KG("KiloGrams"),
    Day("Days"),
    Week("Weeks"),
    Month("Months"),
    Year("Year"),
    Other("Other");

    override fun toString(): String = label
}

/** Time window shown on the dashboard chart and list. */
enum class TimeRange(val label: String, val days: Int) {
    Day("1D", 0),
    Week("1W", 7),
    Month("1M", 30),
    SixMonths("6M", 180),

    /** Everything since the first record of the visible tasks; [days] is unused. */
    Max("Max", 0);
}

/** Weekly reminder configuration of a task. An empty [days] set means every day. */
data class Reminder(
    val time: LocalTime,
    val days: Set<DayOfWeek>,
)

data class Task(
    val id: Long = 0,
    val name: String,
    val type: EntryType,
    val unit: MeasureUnit,
    /** ARGB color used to draw the task on charts. */
    val color: Int,
    val reminder: Reminder? = null,
) {
    val displayUnit: String? get() = if (type == EntryType.Number && unit != MeasureUnit.Other) unit.label else null
}

/** A single logged value of a [task]. */
data class TaskRecord(
    val task: Task,
    /** Epoch milliseconds of the event. Also the record's identity. */
    val time: Long,
    /** Raw value: "1"/"0" for Yes/No tasks, a decimal number for Number tasks. */
    val value: String,
    val comment: String,
) {
    val numericValue: Float get() = value.toFloatOrNull() ?: 0f

    val displayValue: String
        get() = when (task.type) {
            EntryType.YesNo -> if (value.toFloatOrNull()?.toInt() == 1) "True" else "False"
            EntryType.Number -> listOfNotNull(value, task.displayUnit).joinToString(" ")
        }
}
