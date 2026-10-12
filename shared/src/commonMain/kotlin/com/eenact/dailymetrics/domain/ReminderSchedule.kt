package com.eenact.dailymetrics.domain

import com.eenact.dailymetrics.domain.model.Reminder
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object ReminderSchedule {

    /**
     * The next moment strictly after [now] at which [reminder] should fire. Days are honoured when
     * the reminder has any; otherwise it fires every day.
     */
    fun nextTrigger(reminder: Reminder, now: Instant, zone: TimeZone): Instant {
        val today = now.toLocalDateTime(zone).date
        for (offset in 0..7) {
            val date = today.plus(DatePeriod(days = offset))
            if (reminder.days.isNotEmpty() && date.dayOfWeek !in reminder.days) continue
            val candidate = date.atTime(reminder.time).toInstant(zone)
            if (candidate > now) return candidate
        }
        // Unreachable for a valid reminder: a matching weekday occurs within any 8 day window.
        return today.plus(DatePeriod(days = 1)).atTime(reminder.time).toInstant(zone)
    }
}
