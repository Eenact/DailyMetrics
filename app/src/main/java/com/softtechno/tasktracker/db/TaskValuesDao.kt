package com.softtechno.tasktracker.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskValuesDao {

    @Query("SELECT * FROM TaskValue")
    fun getAll(): Flow<List<TaskValue>>

    @Query("SELECT * FROM TaskValue WHERE time >= :startTime AND time <= :endTime")
    fun getTaskBetween(startTime: Long, endTime: Long): Flow<List<TaskValue>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskValue(taskValue: TaskValue)

    @Query("DELETE FROM TaskValue")
    suspend fun deleteAll()

    @Delete(entity = TaskValue::class)
    suspend fun deleteTaskValue(taskValue: TaskValue)

}