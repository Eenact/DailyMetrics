package com.softtechno.tasktracker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.softtechno.tasktracker.enums.EntityType

@Entity
class TaskEntity (

    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "type") val type: EntityType,

    @PrimaryKey(autoGenerate = true) val id: Int? = null

)