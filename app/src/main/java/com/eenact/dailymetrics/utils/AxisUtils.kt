package com.eenact.dailymetrics.utils

import com.eenact.dailymetrics.enums.AxisXTypes
import java.util.*

class AxisUtils {

    companion object {

        fun getTimeAxisLabels(
            selectedCalendar: Calendar, selection: AxisXTypes
        ): Map<Int, String> {

            val countLabels = getGranularity(selection)
            val weekLabelMap: MutableMap<Int, String> = mutableMapOf()

            selectedCalendar.add(Calendar.DAY_OF_YEAR, 1)
            selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
            selectedCalendar.set(Calendar.MINUTE, 0)
            selectedCalendar.set(Calendar.SECOND, 0)

            when (selection) {

                AxisXTypes.Day -> {
                    selectedCalendar.add(Calendar.DAY_OF_YEAR, -1)
                    for (day in 1..countLabels) {
                        weekLabelMap[selectedCalendar.get(Calendar.DAY_OF_YEAR) * 100 + selectedCalendar.get(
                            Calendar.HOUR_OF_DAY
                        )] = DateTimeUtils.axisLabelDay(
                            selectedCalendar.get(
                                Calendar.HOUR_OF_DAY
                            )
                        )
                        selectedCalendar.add(Calendar.HOUR_OF_DAY, 2)
                    }
                }

                AxisXTypes.ThreeDays -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[(selectedCalendar.timeInMillis / 1000).toInt()] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ), selectedCalendar.get(
                                    Calendar.MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, -1)
                    }
                }

                AxisXTypes.Week -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[(selectedCalendar.timeInMillis / 1000).toInt()] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ), selectedCalendar.get(
                                    Calendar.MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, -2)
                    }
                }

                AxisXTypes.Month -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[(selectedCalendar.timeInMillis / 1000).toInt()] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ), selectedCalendar.get(
                                    Calendar.MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, -7)
                    }
                }

                AxisXTypes.ThreeMonths -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[(selectedCalendar.timeInMillis / 1000).toInt()] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ), selectedCalendar.get(
                                    Calendar.MONTH
                                )
                            )
                        selectedCalendar.add(Calendar.DAY_OF_YEAR, -18)
                    }
                }

                AxisXTypes.SixMonths -> {
                    for (day in 1..countLabels) {
                        weekLabelMap[(selectedCalendar.timeInMillis / 1000).toInt()] =
                            DateTimeUtils.axisLabelDate(
                                selectedCalendar.get(
                                    Calendar.DAY_OF_MONTH
                                ), selectedCalendar.get(
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
                AxisXTypes.ThreeDays -> 4
                AxisXTypes.Week -> 5
                AxisXTypes.Month -> 5
                AxisXTypes.ThreeMonths -> 5
                AxisXTypes.SixMonths -> 5
            }
        }

        fun getTimeAxisValue(time: Long, selection: AxisXTypes): Float {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time

            val timestamp: Int = (time / 1000).toInt()

            return when (selection) {
                AxisXTypes.Day -> ((calendar.get(Calendar.DAY_OF_YEAR) * 100) + calendar.get(
                    Calendar.HOUR_OF_DAY
                ))
                AxisXTypes.ThreeDays -> timestamp
                AxisXTypes.Week -> timestamp
                AxisXTypes.Month -> timestamp
                AxisXTypes.ThreeMonths -> timestamp
                AxisXTypes.SixMonths -> timestamp
            }.toFloat()

        }

    }

}