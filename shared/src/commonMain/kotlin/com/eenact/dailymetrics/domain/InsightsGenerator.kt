package com.eenact.dailymetrics.domain

import com.eenact.dailymetrics.domain.model.EntryType
import com.eenact.dailymetrics.domain.model.TaskRecord
import kotlin.math.roundToInt

/**
 * On-device summary shown in the "AI Suggestions" card.
 *
 * This is a deterministic, offline placeholder. A model-backed implementation can replace it later
 * without touching the UI (see docs/BACKLOG.md).
 */
object InsightsGenerator {

    private const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000

    fun generate(records: List<TaskRecord>, nowMillis: Long): List<String> {
        val thisWeek = records.filter { it.time > nowMillis - WEEK_MILLIS }
        val lastWeek = records.filter { it.time in (nowMillis - 2 * WEEK_MILLIS)..(nowMillis - WEEK_MILLIS) }
        if (thisWeek.isEmpty()) {
            return listOf("Not enough recent data yet. Log entries for a few days to get insights.")
        }

        return thisWeek.groupBy { it.task.id }.values.map { taskRecords ->
            val task = taskRecords.first().task
            val previous = lastWeek.filter { it.task.id == task.id }
            when (task.type) {
                EntryType.YesNo -> {
                    val yes = taskRecords.count { it.numericValue.roundToInt() == 1 }
                    val rate = yes * 100 / taskRecords.size
                    val trend = previous.takeIf { it.isNotEmpty() }?.let { prev ->
                        val prevRate = prev.count { it.numericValue.roundToInt() == 1 } * 100 / prev.size
                        trend(rate.toFloat(), prevRate.toFloat())
                    }.orEmpty()
                    "${task.name}: \"Yes\" on $yes of ${taskRecords.size} entries this week ($rate%)$trend."
                }

                EntryType.Number -> {
                    val average = taskRecords.map { it.numericValue }.average().toFloat()
                    val unit = task.displayUnit?.let { " $it" }.orEmpty()
                    val trend = previous.takeIf { it.isNotEmpty() }?.let { prev ->
                        trend(average, prev.map { it.numericValue }.average().toFloat())
                    }.orEmpty()
                    "${task.name}: average ${format(average)}$unit over ${entries(taskRecords.size)} this week$trend."
                }
            }
        }
    }

    private fun entries(count: Int): String = if (count == 1) "1 entry" else "$count entries"

    private fun trend(current: Float, previous: Float): String {
        if (previous == 0f) return ""
        val change = ((current - previous) / previous * 100).roundToInt()
        return when {
            change > 0 -> ", up $change% from last week"
            change < 0 -> ", down ${-change}% from last week"
            else -> ", same as last week"
        }
    }

    private fun format(value: Float): String {
        val rounded = (value * 10).roundToInt()
        return if (rounded % 10 == 0) (rounded / 10).toString() else "${rounded / 10}.${rounded % 10}"
    }
}
