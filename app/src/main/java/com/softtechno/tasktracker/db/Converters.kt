package com.softtechno.tasktracker.db

import androidx.room.TypeConverter
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = value
        return calendar.time
    }

}