package com.eenact.dailymetrics.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class CsvExporterTest {

    @Test
    fun writesHeaderAndRowsInTheOriginalFormat() {
        val file = CsvExporter.export(
            records = listOf(
                record(numberTask, millis(2026, 9, 28, 21, 49), "7.5", "good night"),
                record(yesNoTask, millis(2026, 9, 27, 8, 5), "1"),
            ),
            isFiltered = false,
            nowMillis = millis(2026, 9, 28, 22, 0),
            zone = UTC,
        )

        assertEquals(
            listOf(
                "Date,Task,Unit,Value,\"Comments\"",
                "2026.09.27 08:05,Exercise,Yes/No,1,\"\"",
                "2026.09.28 21:49,Sleep,Hours,7.5,\"good night\"",
            ),
            file.content.lines(),
        )
        assertEquals("DailyMetrics_all_2026_09_28-22_00_00.csv", file.fileName)
    }

    @Test
    fun escapesCommasAndQuotes() {
        val task = numberTask.copy(name = "Run, walk")
        val file = CsvExporter.export(
            listOf(record(task, millis(2026, 1, 2, 3, 4), "1.0", "said \"hi\"")),
            isFiltered = true,
            nowMillis = millis(2026, 1, 2, 3, 4),
            zone = UTC,
        )
        assertEquals("2026.01.02 03:04,\"Run, walk\",Hours,1.0,\"said \"\"hi\"\"\"", file.content.lines()[1])
        assertEquals("DailyMetrics_filtered_2026_01_02-03_04_00.csv", file.fileName)
    }
}
