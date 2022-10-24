package com.softtechno.tasktracker.db

import android.content.Context
import kotlinx.coroutines.flow.Flow

class TaskRepository {

    private val database: AppDatabase

    constructor(context: Context) {
        database = AppDatabase.getDatabase(context)
    }

    // TaskValue functions
    fun getAllTaskValues(): Flow<Map<TaskEntity, TaskValue>> = database.taskValuesDao().getAll()

    fun getAllTaskValuesBetween(taskId: Int, start: Long, end: Long): Flow<Map<TaskEntity, List<TaskValue>>> = database.taskValuesDao().getTaskBetween(taskId, startTime = start, endTime = end)

    fun getAllTask(taskId: Int): Flow<List<TaskValue>> = database.taskValuesDao().getTaskAll(taskId)

    suspend fun insertTaskValue(taskValue: TaskValue) = database.taskValuesDao().insertTaskValue(taskValue)

    suspend fun deleteAllTaskValues() = database.taskValuesDao().deleteAll()

    suspend fun deleteTaskValue(taskValue: TaskValue) = database.taskValuesDao().deleteTaskValue(taskValue)

    //TaskEntity functions
    fun getAllTaskEntities(): Flow<List<TaskEntity>> = database.taskEntitiesDao().getAll()

    suspend fun getTaskEntity(taskId: Int): TaskEntity = database.taskEntitiesDao().get(taskId)

    suspend fun containTask(taskName: String): Boolean = database.taskEntitiesDao().containTask(taskName)

    suspend fun insertTaskEntity(taskEntity: TaskEntity) = database.taskEntitiesDao().insertEntity(taskEntity)

    suspend fun updateTaskEntity(taskEntity: TaskEntity) = database.taskEntitiesDao().updateEntity(taskEntity)

    suspend fun deleteTaskEntity(taskEntity: TaskEntity) = database.taskEntitiesDao().deleteEntity(taskEntity)
}