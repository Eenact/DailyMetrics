package com.eenact.dailymetrics.di

import androidx.activity.ComponentActivity
import com.eenact.dailymetrics.data.RoomTaskRepository
import com.eenact.dailymetrics.platform.ActivityResultBridge
import com.eenact.dailymetrics.platform.AndroidAppSettings
import com.eenact.dailymetrics.platform.AndroidDatabase
import com.eenact.dailymetrics.platform.AndroidFileExporter
import com.eenact.dailymetrics.platform.AndroidNotificationScheduler
import com.eenact.dailymetrics.platform.androidAppInfo

/** Builds the [AppContainer] for [activity]. Call from `onCreate`. */
fun createAndroidAppContainer(activity: ComponentActivity): AppContainer {
    val bridge = ActivityResultBridge(activity)
    return AppContainer(
        repository = RoomTaskRepository(AndroidDatabase.get(activity)),
        settings = AndroidAppSettings(activity),
        notificationScheduler = AndroidNotificationScheduler(activity, bridge::requestNotificationPermission),
        fileExporter = AndroidFileExporter(activity, bridge),
        appInfo = androidAppInfo(activity),
    )
}
