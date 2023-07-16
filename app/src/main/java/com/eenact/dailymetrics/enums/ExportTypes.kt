package com.eenact.dailymetrics.enums

enum class ExportTypes(private val position: Int) {
    ShareData(0), ExportData(1);

    companion object {
        fun fromPosition(position: Int): ExportTypes {
            return when (position) {
                ExportData.position -> ExportData
                else -> ShareData
            }
        }
    }
}