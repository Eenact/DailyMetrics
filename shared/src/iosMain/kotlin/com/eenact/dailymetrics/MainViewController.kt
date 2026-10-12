package com.eenact.dailymetrics

import androidx.compose.ui.window.ComposeUIViewController
import com.eenact.dailymetrics.data.RoomTaskRepository
import com.eenact.dailymetrics.di.AppContainer
import com.eenact.dailymetrics.platform.IosAppSettings
import com.eenact.dailymetrics.platform.IosFileExporter
import com.eenact.dailymetrics.platform.IosNotificationScheduler
import com.eenact.dailymetrics.platform.createIosDatabase
import com.eenact.dailymetrics.platform.iosAppInfo
import platform.UIKit.UIViewController

private val container: AppContainer by lazy {
    AppContainer(
        repository = RoomTaskRepository(createIosDatabase()),
        settings = IosAppSettings(),
        notificationScheduler = IosNotificationScheduler(),
        fileExporter = IosFileExporter(),
        appInfo = iosAppInfo(),
    )
}

/** Entry point called from Swift (`MainViewControllerKt.MainViewController()`). */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController = ComposeUIViewController { DailyMetricsApp(container) }
