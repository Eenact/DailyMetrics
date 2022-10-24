package com.softtechno.tasktracker.utils

import android.content.Context
import com.softtechno.tasktracker.enums.AxisXTypes
import java.util.*

class AxisUtils {

    companion object {

        fun getTimeAxisLabels(
            selectedCalendar: Calendar,
            selection: AxisXTypes,
            context: Context
        ): Map<Int, String> {

            val weekLabelMap: MutableMap<Int, String> = mutableMapOf()

            when (selection) {

                AxisXTypes.Day -> {
                    for (day in 1..12) {
                        selectedCalendar.add(Calendar.HOUR_OF_DAY, 2)
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR) * 100 + selectedCalendar.get(Calendar.HOUR_OF_DAY)] =
                            DateTimeUtils.axisLabelDay(
                                selectedCalendar.get(
                                    Calendar.HOUR_OF_DAY
                                )
                            )
                    }
                }

                AxisXTypes.Week -> {
                    for (day in 1..7) {
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, 1)
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR)] =
                            DateTimeUtils.axisLabelWeek(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_WEEK
                                ), context
                            )
                    }
                }

                AxisXTypes.Month -> {
                    val now = Calendar.getInstance()
                    for (day in 1..15) {
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, 2)
                        if (selectedCalendar > now) {
                            break
                        }
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR)] =
                            DateTimeUtils.axisLabelMonth(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                )
                            )
                    }
                }

                AxisXTypes.Year -> {
                    for (day in 1..12) {
                        selectedCalendar.add(Calendar.MONTH, 1)
                        weekLabelMap[selectedCalendar.get(Calendar.MONTH)] =
                            DateTimeUtils.axisLabelYear(
                                selectedCalendar.get(
                                    Calendar.MONTH
                                ), context
                            )
                    }
                }

            }

            return weekLabelMap

        }

        fun getTimeAxisValue(time: Long, selection: AxisXTypes): Float {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time

            return when (selection) {
                AxisXTypes.Day -> ((calendar.get(Calendar.DAY_OF_YEAR) * 100) + calendar.get(Calendar.HOUR_OF_DAY))
                AxisXTypes.Week -> calendar.get(Calendar.DAY_OF_YEAR)
                AxisXTypes.Month -> calendar.get(Calendar.DAY_OF_YEAR)
                AxisXTypes.Year -> calendar.get(Calendar.MONTH)
            }.toFloat()

        }

    }

}