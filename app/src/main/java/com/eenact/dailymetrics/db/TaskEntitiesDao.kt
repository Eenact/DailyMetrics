package com.eenact.dailymetrics.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskEntitiesDao {

    @Query("SELECT * FROM TaskEntity")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM TaskEntity WHERE id = :taskId")
    suspend fun get(taskId: Int): TaskEntity

    @Query("SELECT EXISTS(SELECT * FROM TaskEntity WHERE name = :taskName)")
    suspend fun containTask(taskName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntity(entity: TaskEntity)

    @Update(entity = TaskEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEntity(vararg entity: TaskEntity): Int

    @Delete(entity = TaskEntity::class)
    suspend fun deleteEntity(vararg entity: TaskEntity)

}