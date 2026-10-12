package com.eenact.dailymetrics.data.db

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.eenact.dailymetrics.domain.model.EntryType
import com.eenact.dailymetrics.domain.model.MeasureUnit

// Table and column names mirror the schema shipped by the original Android app (DB version 3).
// Changing any of them requires a migration, otherwise existing installs lose their data.

@Entity(tableName = "TaskEntity")
data class TaskEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: EntryType,
    @ColumnInfo(name = "unit") val unit: MeasureUnit,
    @ColumnInfo(name = "color") val color: Int,
    @ColumnInfo(name = "is_notification_enabled") val isNotificationEnabled: Boolean = false,
    /** "HH:mm" in 24h format. */
    @ColumnInfo(name = "notification_time") val notificationTime: String? = null,
    /** Comma separated java.util.Calendar day numbers (Sunday = 1 … Saturday = 7). */
    @ColumnInfo(name = "notification_days") val notificationDays: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)

@Entity(
    tableName = "TaskValue",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("task_id")],
)
data class TaskValueEntity(
    @PrimaryKey val time: Long,
    @ColumnInfo(name = "task_id") val taskId: Long,
    @ColumnInfo(name = "task_value") val taskValue: String,
    @ColumnInfo(name = "comments") val comments: String = "",
    /** Legacy numeric column from DB version 1, kept for schema compatibility. */
    @ColumnInfo(name = "value") val legacyValue: Long = -1,
)

data class TaskValueWithTask(
    @Embedded val value: TaskValueEntity,
    @Embedded val task: TaskEntity,
)
