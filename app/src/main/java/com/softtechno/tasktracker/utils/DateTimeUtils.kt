package com.softtechno.tasktracker.utils

import android.content.Context
import com.softtechno.tasktracker.R
import java.util.*

class DateTimeUtils {

    companion object {

        fun resetTime(calendar: Calendar) {

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

        }

        fun axisLabelDay(hour: Int): String {
            return "${hour}h"
        }

        fun axisLabelDate(day: Int, month: Int): String {
            return "$day/$month"
        }

        fun axisLabelWeek(day: Int, context: Context): String {

            return when (day) {

                Calendar.SATURDAY -> context.getString(R.string.short_saturday)
                Calendar.SUNDAY -> context.getString(R.string.short_sunday)
                Calendar.MONDAY -> context.getString(R.string.short_monday)
                Calendar.TUESDAY -> context.getString(R.string.short_tuesday)
                Calendar.WEDNESDAY -> context.getString(R.string.short_wednesday)
                Calendar.THURSDAY -> context.getString(R.string.short_thursday)
                else -> context.getString(R.string.short_friday)

            }

        }

        fun axisLabelMonth(day: Int): String {
            return "$day"
        }

        fun axisLabelYear(month: Int, context: Context): String {
            return when (month) {
                Calendar.JANUARY -> context.getString(R.string.short_january)
                Calendar.FEBRUARY -> context.getString(R.string.short_february)
                Calendar.MARCH -> context.getString(R.string.short_march)
                Calendar.APRIL -> context.getString(R.string.short_april)
                Calendar.MAY -> context.getString(R.string.short_may)
                Calendar.JUNE -> context.getString(R.string.short_june)
                Calendar.JULY -> context.getString(R.string.short_july)
                Calendar.AUGUST -> context.getString(R.string.short_august)
                Calendar.SEPTEMBER -> context.getString(R.string.short_september)
                Calendar.OCTOBER -> context.getString(R.string.short_october)
                Calendar.NOVEMBER -> context.getString(R.string.short_november)
                Calendar.DECEMBER -> context.getString(R.string.short_december)
                else -> ""
            }
        }

    }

}