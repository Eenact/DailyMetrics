package com.eenact.dailymetrics.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.eenact.dailymetrics.data.TaskRepository
import com.eenact.dailymetrics.platform.AppInfo
import com.eenact.dailymetrics.platform.AppSettings
import com.eenact.dailymetrics.platform.FileExporter
import com.eenact.dailymetrics.platform.NotificationScheduler

/** Manual dependency container. Each platform builds one and passes it to [com.eenact.dailymetrics.DailyMetricsApp]. */
class AppContainer(
    val repository: TaskRepository,
    val settings: AppSettings,
    val notificationScheduler: NotificationScheduler,
    val fileExporter: FileExporter,
    val appInfo: AppInfo,
)

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
