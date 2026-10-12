package com.eenact.dailymetrics.domain

import com.eenact.dailymetrics.domain.model.EntryType
import com.eenact.dailymetrics.domain.model.TaskRecord
import kotlinx.datetime.TimeZone

data class CsvFile(val fileName: String, val content: String) {
    companion object {
        const val MIME_TYPE = "text/comma-separated-values"
    }
}

/** Produces the same CSV layout as the original Android app. */
object CsvExporter {

    fun export(
        records: List<TaskRecord>,
        isFiltered: Boolean,
        nowMillis: Long,
        zone: TimeZone = TimeZone.currentSystemDefault(),
    ): CsvFile {
        val header = row("Date", "Task", "Unit", "Value", "Comments")
        val rows = records.sortedBy { it.time }.map { record ->
            row(
                DateTimeFormatting.csvTimestamp(record.time, zone),
                record.task.name,
                if (record.task.type == EntryType.YesNo) EntryType.YesNo.label else record.task.unit.label,
                record.value,
                record.comment,
            )
        }
        val content = (listOf(header) + rows).joinToString("\n")

        val stamp = DateTimeFormatting.fileStamp(nowMillis, zone)
        val fileName = if (isFiltered) "DailyMetrics_filtered_$stamp.csv" else "DailyMetrics_all_$stamp.csv"
        return CsvFile(fileName, content)
    }

    private fun row(date: String, name: String, unit: String, value: String, comment: String): String =
        listOf(escape(date), escape(name), escape(unit), escape(value), quote(comment)).joinToString(",")

    private fun escape(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' }) quote(field) else field

    // Comments were always quoted by the original exporter.
    private fun quote(field: String): String = "\"" + field.replace("\"", "\"\"") + "\""
}
