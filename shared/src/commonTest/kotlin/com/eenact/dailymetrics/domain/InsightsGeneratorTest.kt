package com.eenact.dailymetrics.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InsightsGeneratorTest {

    private val now = millis(2026, 9, 28, 21, 0)

    @Test
    fun asksForMoreDataWhenThereIsNothingRecent() {
        val insights = InsightsGenerator.generate(listOf(record(yesNoTask, millis(2026, 8, 1), "1")), now)
        assertEquals(1, insights.size)
        assertTrue(insights.single().startsWith("Not enough recent data"))
    }

    @Test
    fun summarisesYesNoAndNumberTasksWithTrend() {
        val records = listOf(
            record(yesNoTask, millis(2026, 9, 27), "1"),
            record(yesNoTask, millis(2026, 9, 26), "0"),
            record(numberTask, millis(2026, 9, 27), "8"),
            record(numberTask, millis(2026, 9, 18), "6"), // previous week
        )
        val insights = InsightsGenerator.generate(records, now)
        assertEquals("Exercise: \"Yes\" on 1 of 2 entries this week (50%).", insights[0])
        assertEquals("Sleep: average 8 Hours over 1 entry this week, up 33% from last week.", insights[1])
    }
}
