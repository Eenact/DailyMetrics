package com.softtechno.tasktracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskValuesDao {

    @Query("SELECT * FROM TaskValue")
    suspend fun getAll(): List<TaskValue>

    @Query("SELECT * FROM TaskValue WHERE time >= :startTime AND time <= :endTime")
    suspend fun getTaskBetween(startTime: Long, endTime: Long): List<TaskValue>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskValue(taskValue: TaskValue)

}