package com.softtechno.tasktracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskEntitiesDao {

    @Query("SELECT * FROM TaskEntity")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM TaskEntity WHERE id = :taskId")
    suspend fun get(taskId: Int): TaskEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: TaskEntity)

}