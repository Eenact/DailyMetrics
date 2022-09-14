package com.softtechno.tasktracker.db

import androidx.room.*

@Dao
interface TaskValuesDao {

    @Query("SELECT * FROM TaskValue")
    suspend fun getAll(): List<TaskValue>

    @Query("SELECT * FROM TaskValue WHERE time >= :startTime AND time <= :endTime")
    suspend fun getTaskBetween(startTime: Long, endTime: Long): List<TaskValue>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskValue(taskValue: TaskValue)

    @Query("DELETE FROM TaskValue")
    suspend fun deleteAll()

    @Delete(entity = TaskValue::class)
    suspend fun deleteTaskValue(taskValue: TaskValue)

}