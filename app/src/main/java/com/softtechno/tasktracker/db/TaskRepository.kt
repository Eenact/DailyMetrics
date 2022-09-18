package com.softtechno.tasktracker.db

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class TaskRepository {

    private val database: AppDatabase

    constructor(db: AppDatabase) {
        database = db
    }

    constructor(context: Context) {
        database = AppDatabase.getDatabase(context)
    }

    // TaskValue functions
    fun getAllTaskValues(): LiveData<List<TaskValue>> = database.taskValuesDao().getAll()

    fun getAllTaskValuesBetween(start: Long, end: Long): LiveData<List<TaskValue>> = database.taskValuesDao().getTaskBetween(startTime = start, endTime = end)

    suspend fun insertTaskValue(taskValue: TaskValue) = database.taskValuesDao().insertTaskValue(taskValue)

    suspend fun deleteAllTaskValues() = database.taskValuesDao().deleteAll()

    suspend fun deleteTaskValue(taskValue: TaskValue) = database.taskValuesDao().deleteTaskValue(taskValue)

    //TaskEntity functions
    fun getAllTaskEntities(): Flow<List<TaskEntity>> = database.taskEntitiesDao().getAll()

    suspend fun getTaskEntity(taskId: Int): TaskEntity = database.taskEntitiesDao().get(taskId)

    suspend fun insertTaskEntity(taskEntity: TaskEntity) = database.taskEntitiesDao().insertEntity(taskEntity)

    suspend fun updateTaskEntity(taskEntity: TaskEntity) = database.taskEntitiesDao().updateEntity(taskEntity)

    suspend fun deleteTaskEntity(taskEntity: TaskEntity) = database.taskEntitiesDao().deleteEntity(taskEntity)
}