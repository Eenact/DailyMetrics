package com.softtechno.tasktracker.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskValuesDao {

    @Query("SELECT * FROM TaskEntity JOIN TaskValue ON TaskEntity.id = TaskValue.task_id")
    fun getAll(): Flow<Map<TaskEntity, List<TaskValue>>>

    @Query("SELECT * FROM TaskEntity JOIN TaskValue ON TaskEntity.id = TaskValue.task_id WHERE TaskEntity.id = :taskId AND time >= :startTime AND time <= :endTime")
    fun getTaskBetween(taskId: Int, startTime: Long, endTime: Long): Flow<Map<TaskEntity, List<TaskValue>>>

    @Query("SELECT * FROM TaskValue WHERE task_id = :taskId")
    fun getTaskAll(taskId: Int): Flow<List<TaskValue>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskValue(taskValue: TaskValue)

    @Query("DELETE FROM TaskValue")
    suspend fun deleteAll()

    @Delete(entity = TaskValue::class)
    suspend fun deleteTaskValue(taskValue: TaskValue)

}