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

            val countLabels = getGranularity(selection)
            val weekLabelMap: MutableMap<Int, String> = mutableMapOf()

            when (selection) {

                AxisXTypes.Day -> {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
                    for (day in 1..countLabels) {
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR) * 100 + selectedCalendar.get(
                            Calendar.HOUR_OF_DAY
                        )] =
                            DateTimeUtils.axisLabelDay(
                                selectedCalendar.get(
                                    Calendar.HOUR_OF_DAY
                                )
                            )
                        selectedCalendar.add(Calendar.HOUR_OF_DAY, 2)
                    }
                }

                AxisXTypes.Week -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR)] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ),
                                selectedCalendar.get(
                                    Calendar.MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, -2)
                    }
                }

                AxisXTypes.Month -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR)] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ),
                                selectedCalendar.get(
                                    Calendar.MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, -6)
                    }
                }

                AxisXTypes.ThreeMonths -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR)] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ),
                                selectedCalendar.get(
                                    Calendar.MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, -18)
                    }
                }

                AxisXTypes.SixMonths -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR)] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ),
                                selectedCalendar.get(
                                    Calendar.MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, -36)
                    }
                }

            }

            return weekLabelMap

        }

        fun getGranularity(selection: AxisXTypes): Int {
            return when (selection) {
                AxisXTypes.Day -> 24 / 2
                AxisXTypes.Week -> 4
                AxisXTypes.Month -> 5
                AxisXTypes.ThreeMonths -> 5
                AxisXTypes.SixMonths -> 5
            }
        }

        fun getTimeAxisValue(time: Long, selection: AxisXTypes): Float {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time

            return when (selection) {
                AxisXTypes.Day -> ((calendar.get(Calendar.DAY_OF_YEAR) * 100) + calendar.get(
                    Calendar.HOUR_OF_DAY
                ))
                AxisXTypes.Week -> calendar.get(Calendar.DAY_OF_YEAR)
                AxisXTypes.Month -> calendar.get(Calendar.DAY_OF_YEAR)
                AxisXTypes.ThreeMonths -> calendar.get(Calendar.DAY_OF_YEAR)
                AxisXTypes.SixMonths -> calendar.get(Calendar.DAY_OF_YEAR)
            }.toFloat()

        }

    }

}