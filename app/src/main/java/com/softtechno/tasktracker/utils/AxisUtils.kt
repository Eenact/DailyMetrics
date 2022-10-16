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
                    for (day in 1..24) {
                        weekLabelMap[selectedCalendar.get(Calendar.HOUR_OF_DAY)] =
                            DateTimeUtils.axisLabelDay(
                                selectedCalendar.get(
                                    Calendar.HOUR_OF_DAY
                                )
                            )
                        selectedCalendar.add(Calendar.HOUR_OF_DAY, 1)
                    }
                }

                AxisXTypes.Week -> {
                    for (day in 1..7) {
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_WEEK)] =
                            DateTimeUtils.axisLabelWeek(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_WEEK
                                ), context
                            )
                        selectedCalendar.add(Calendar.DAY_OF_WEEK, 1)
                    }
                }

                AxisXTypes.Month -> {
                    /*for (day in 1..7) {
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_MONTH)] =
                            DateTimeUtils.axisLabelMonth(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    }*/
                }

                AxisXTypes.Year -> {
                    for (day in 1..12) {
                        weekLabelMap[selectedCalendar.get(Calendar.MONTH)] =
                            DateTimeUtils.axisLabelWeek(
                                selectedCalendar.get(
                                    Calendar.MONTH
                                ), context
                            )
                        selectedCalendar.add(Calendar.MONTH, 1)
                    }
                }

            }

            return weekLabelMap

        }

        fun getTimeAxisValue(time: Long, selection: AxisXTypes): Float {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time

            return when (selection) {
                AxisXTypes.Day -> calendar.get(Calendar.HOUR_OF_DAY)
                AxisXTypes.Week -> calendar.get(Calendar.DAY_OF_WEEK)
                AxisXTypes.Month -> calendar.get(Calendar.DAY_OF_MONTH)
                AxisXTypes.Year -> calendar.get(Calendar.MONTH)
            }.toFloat()

        }

    }

}