package com.eenact.dailymetrics.data.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM TaskEntity ORDER BY id")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM TaskEntity ORDER BY id")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM TaskEntity WHERE id = :taskId")
    suspend fun getById(taskId: Long): TaskEntity?

    @Query("SELECT EXISTS(SELECT * FROM TaskEntity WHERE name = :name)")
    suspend fun exists(name: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TaskEntity): Long

    @Update
    suspend fun update(entity: TaskEntity): Int

    @Delete
    suspend fun delete(entity: TaskEntity)
}

@Dao
interface TaskValueDao {

    @Query(
        "SELECT * FROM TaskValue JOIN TaskEntity ON TaskEntity.id = TaskValue.task_id " +
            "ORDER BY TaskValue.time"
    )
    fun observeAllWithTask(): Flow<List<TaskValueWithTask>>

    /** Returns the row id, or -1 when a value with the same timestamp already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(value: TaskValueEntity): Long

    @Query("DELETE FROM TaskValue WHERE time = :time")
    suspend fun deleteByTime(time: Long)
}
