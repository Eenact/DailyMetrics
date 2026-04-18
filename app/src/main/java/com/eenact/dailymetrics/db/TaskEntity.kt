package com.eenact.dailymetrics.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eenact.dailymetrics.enums.EntityType
import com.eenact.dailymetrics.enums.Units

@Entity
class TaskEntity (

    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "type") val type: EntityType,

    @ColumnInfo(name = "unit") val unit: Units,

    @ColumnInfo(name = "color") val color: Int,

    @ColumnInfo(name = "is_notification_enabled") val isNotificationEnabled: Boolean = false,

    @ColumnInfo(name = "notification_time") val notificationTime: String? = null,

    @ColumnInfo(name = "notification_days") val notificationDays: String? = null,

    @PrimaryKey(autoGenerate = true) val id: Long = 0

)