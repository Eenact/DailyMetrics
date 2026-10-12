package com.eenact.dailymetrics.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eenact.dailymetrics.data.RoomTaskRepository
import com.eenact.dailymetrics.platform.AndroidDatabase
import com.eenact.dailymetrics.platform.AndroidNotificationScheduler
import com.eenact.dailymetrics.platform.ReminderAlarms
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.notification_channel_description
import com.eenact.dailymetrics.resources.notification_channel_name
import com.eenact.dailymetrics.resources.notification_text
import com.eenact.dailymetrics.resources.notification_title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

private const val CHANNEL_ID = "task_reminders"

/** Runs [block] off the main thread while keeping the broadcast alive until it finishes. */
private fun BroadcastReceiver.launchAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        try {
            block()
        } finally {
            pending.finish()
        }
    }
}

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(ReminderAlarms.EXTRA_TASK_ID, -1)
        val taskName = intent.getStringExtra(ReminderAlarms.EXTRA_TASK_NAME) ?: return
        val reminder = ReminderAlarms.reminderFrom(intent)

        // Arm the next occurrence first so a failure below cannot stop future reminders.
        if (reminder != null) ReminderAlarms.scheduleNext(context, taskId, taskName, reminder)

        launchAsync { showNotification(context, taskId, taskName) }
    }

    private suspend fun showNotification(context: Context, taskId: Long, taskName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, getString(Res.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = getString(Res.string.notification_channel_description) }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(context, taskId.toInt(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(getString(Res.string.notification_title))
            .setContentText(getString(Res.string.notification_text, taskName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        } catch (_: SecurityException) {
            // Notification permission revoked; nothing to show.
        }
    }
}

/** Alarms do not survive a reboot or an app update, so re-arm every reminder. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        launchAsync {
            val scheduler = AndroidNotificationScheduler(context)
            RoomTaskRepository(AndroidDatabase.get(context)).getTasks()
                .filter { it.reminder != null }
                .forEach(scheduler::schedule)
        }
    }
}
