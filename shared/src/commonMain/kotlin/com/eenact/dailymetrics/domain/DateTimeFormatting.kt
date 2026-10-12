package com.eenact.dailymetrics.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object DateTimeFormatting {

    /** e.g. "Sep 28, 2026" */
    private val dateFormat = LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); day(Padding.NONE); chars(", "); year()
    }

    /** e.g. "09:49 PM" */
    private val timeFormat = LocalTime.Format {
        amPmHour(); char(':'); minute(); char(' '); amPmMarker("AM", "PM")
    }

    /** CSV timestamp, e.g. "2026.09.28 21:49" */
    private val csvFormat = LocalDateTime.Format {
        year(); char('.'); monthNumber(); char('.'); day(); char(' '); hour(); char(':'); minute()
    }

    /** File name stamp, e.g. "2026_09_28-21_49_05" */
    private val fileStampFormat = LocalDateTime.Format {
        year(); char('_'); monthNumber(); char('_'); day(); char('-'); hour(); char('_'); minute(); char('_'); second()
    }

    fun date(date: LocalDate): String = date.format(dateFormat)

    fun time(time: LocalTime): String = time.format(timeFormat)

    fun date(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String =
        date(epochMillis.toLocalDateTime(zone).date)

    fun time(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String =
        time(epochMillis.toLocalDateTime(zone).time)

    fun csvTimestamp(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String =
        epochMillis.toLocalDateTime(zone).format(csvFormat)

    fun fileStamp(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String =
        epochMillis.toLocalDateTime(zone).format(fileStampFormat)

    /** 24h "HH:mm", used for reminder times. */
    fun time24(time: LocalTime): String =
        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

fun Long.toLocalDateTime(zone: TimeZone): LocalDateTime =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(zone)
