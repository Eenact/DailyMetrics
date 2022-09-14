package com.softtechno.tasktracker.db

import androidx.room.*

@Dao
interface TaskEntitiesDao {

    @Query("SELECT * FROM TaskEntity")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM TaskEntity WHERE id = :taskId")
    suspend fun get(taskId: Int): TaskEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: TaskEntity)

    @Update(entity = TaskEntity::class)
    suspend fun updateEntity(entity: TaskEntity)

    @Delete(entity = TaskEntity::class)
    suspend fun deleteEntity(entity: TaskEntity)

}