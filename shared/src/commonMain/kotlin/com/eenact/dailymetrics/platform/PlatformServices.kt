package com.eenact.dailymetrics.platform

import com.eenact.dailymetrics.domain.model.Task

// Everything that cannot live in common code is behind one of these interfaces. Android and iOS
// implementations live in the shared module's androidMain / iosMain source sets.

/** Small key-value store for app flags. */
interface AppSettings {
    var isAppOnboarded: Boolean
}

/** Schedules the local "log your task" reminders. */
interface NotificationScheduler {
    /** Asks the OS for permission to post notifications. Returns true when granted. */
    suspend fun requestPermission(): Boolean

    /** Schedules the reminder of [task], or cancels it when the task has no reminder. */
    fun schedule(task: Task)

    fun cancel(taskId: Long)
}

/** Hands an exported file to the OS. */
interface FileExporter {
    /** Opens the system share sheet for a text file. */
    suspend fun share(fileName: String, mimeType: String, content: String)

    /** Lets the user pick where to save a text file. Returns true when the file was written. */
    suspend fun save(fileName: String, mimeType: String, content: String): Boolean
}

data class AppInfo(
    val versionName: String,
    val buildNumber: String,
)
