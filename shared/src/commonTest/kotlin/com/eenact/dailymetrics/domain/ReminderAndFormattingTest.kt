package com.eenact.dailymetrics.domain

import com.eenact.dailymetrics.data.formatDays
import com.eenact.dailymetrics.data.parseDays
import com.eenact.dailymetrics.data.toDomain
import com.eenact.dailymetrics.data.toEntity
import com.eenact.dailymetrics.domain.model.Reminder
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class ReminderAndFormattingTest {

    // Monday 28 Sep 2026, 21:49 UTC
    private val now = Instant.fromEpochMilliseconds(millis(2026, 9, 28, 21, 49))

    @Test
    fun dailyReminderLaterTodayFiresToday() {
        val next = ReminderSchedule.nextTrigger(Reminder(LocalTime(22, 0), emptySet()), now, UTC)
        assertEquals(millis(2026, 9, 28, 22, 0), next.toEpochMilliseconds())
    }

    @Test
    fun dailyReminderEarlierTodayFiresTomorrow() {
        val next = ReminderSchedule.nextTrigger(Reminder(LocalTime(9, 0), emptySet()), now, UTC)
        assertEquals(millis(2026, 9, 29, 9, 0), next.toEpochMilliseconds())
    }

    @Test
    fun weekdayReminderSkipsToTheNextSelectedDay() {
        val reminder = Reminder(LocalTime(9, 0), setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY))
        assertEquals(millis(2026, 10, 1, 9, 0), ReminderSchedule.nextTrigger(reminder, now, UTC).toEpochMilliseconds())
    }

    @Test
    fun weekdayReminderWrapsToNextWeek() {
        val reminder = Reminder(LocalTime(9, 0), setOf(DayOfWeek.MONDAY))
        assertEquals(millis(2026, 10, 5, 9, 0), ReminderSchedule.nextTrigger(reminder, now, UTC).toEpochMilliseconds())
    }

    @Test
    fun legacyCalendarDayStringsRoundTrip() {
        // java.util.Calendar: SUNDAY = 1, MONDAY = 2 … SATURDAY = 7
        assertEquals(setOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.SATURDAY), parseDays("1,2,7"))
        assertEquals("2,3,1", formatDays(setOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY)))
        assertEquals(emptySet(), parseDays(null))
        assertEquals(emptySet(), parseDays(""))
    }

    @Test
    fun taskEntityMappingPreservesReminder() {
        val task = numberTask.copy(reminder = Reminder(LocalTime(7, 5), setOf(DayOfWeek.FRIDAY)))
        val entity = task.toEntity()
        assertEquals("07:05", entity.notificationTime)
        assertEquals("6", entity.notificationDays)
        assertTrue(entity.isNotificationEnabled)
        assertEquals(task, entity.toDomain())
    }

    @Test
    fun formatsDatesLikeTheOriginalApp() {
        assertEquals("Sep 28, 2026", DateTimeFormatting.date(millis(2026, 9, 28, 21, 49), UTC))
        assertEquals("09:49 PM", DateTimeFormatting.time(millis(2026, 9, 28, 21, 49), UTC))
        assertEquals("12:05 AM", DateTimeFormatting.time(millis(2026, 9, 28, 0, 5), UTC))
    }
}
