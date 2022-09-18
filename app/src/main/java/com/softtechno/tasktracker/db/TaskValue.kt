package com.softtechno.tasktracker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("task_id"),
        onDelete = ForeignKey.NO_ACTION
    )]
)
class TaskValue (

    @PrimaryKey val time: Long,

    @ColumnInfo(name = "task_id", index = true) val task_id: Int,

    @ColumnInfo(name = "value") val value: Long

)