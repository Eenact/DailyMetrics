package com.eenact.dailymetrics.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("task_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
class TaskValue (

    @PrimaryKey val time: Long,

    @ColumnInfo(name = "task_id", index = true) val task_id: Long,

    @ColumnInfo(name = "value") val value: Long,

    @ColumnInfo(name = "comments") val comments: String = ""

)