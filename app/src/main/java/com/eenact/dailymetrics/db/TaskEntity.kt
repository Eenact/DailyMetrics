package com.eenact.dailymetrics.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eenact.dailymetrics.enums.EntityType
import com.eenact.dailymetrics.enums.Units

@Entity
class TaskEntity (

    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "type") val type: EntityType,

    @ColumnInfo(name = "unit") val unit: Units,

    @ColumnInfo(name = "color") val color: Int,

    @PrimaryKey(autoGenerate = true) val id: Long = 0

)