package com.softtechno.tasktracker.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskEntitiesDao {

    @Query("SELECT * FROM TaskEntity")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM TaskEntity WHERE id = :taskId")
    suspend fun get(taskId: Int): TaskEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: TaskEntity)

    @Update(entity = TaskEntity::class)
    suspend fun updateEntity(entity: TaskEntity)

    @Delete(entity = TaskEntity::class)
    suspend fun deleteEntity(entity: TaskEntity)

}