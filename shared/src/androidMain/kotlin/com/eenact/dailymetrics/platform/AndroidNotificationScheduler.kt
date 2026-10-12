package com.eenact.dailymetrics.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.eenact.dailymetrics.domain.DateTimeFormatting
import com.eenact.dailymetrics.domain.ReminderSchedule
import com.eenact.dailymetrics.domain.model.Reminder
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.receivers.NotificationReceiver
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

class AndroidNotificationScheduler(
    context: Context,
    private val permissionRequester: (suspend () -> Boolean)? = null,
) : NotificationScheduler {

    private val context = context.applicationContext

    override suspend fun requestPermission(): Boolean = permissionRequester?.invoke() ?: true

    override fun schedule(task: Task) {
        val reminder = task.reminder
        if (reminder == null) cancel(task.id) else ReminderAlarms.scheduleNext(context, task.id, task.name, reminder)
    }

    override fun cancel(taskId: Long) = ReminderAlarms.cancel(context, taskId)
}

/**
 * One exact alarm per task, always set to the task's next occurrence. [NotificationReceiver] posts
 * the notification and arms the following occurrence.
 */
internal object ReminderAlarms {
    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TASK_NAME = "task_name"
    const val EXTRA_TIME = "notification_time"
    const val EXTRA_DAYS = "notification_days"

    fun scheduleNext(context: Context, taskId: Long, taskName: String, reminder: Reminder) {
        val triggerAt = ReminderSchedule.nextTrigger(reminder, Clock.System.now(), TimeZone.currentSystemDefault())
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_NAME, taskName)
            putExtra(EXTRA_TIME, DateTimeFormatting.time24(reminder.time))
            putExtra(EXTRA_DAYS, reminder.days.map { it.ordinal }.toIntArray())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt.toEpochMilliseconds(), pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt.toEpochMilliseconds(), pendingIntent)
        }
    }

    fun cancel(context: Context, taskId: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId.toInt(), Intent(context, NotificationReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun reminderFrom(intent: Intent): Reminder? {
        val time = intent.getStringExtra(EXTRA_TIME)?.split(":")?.mapNotNull { it.toIntOrNull() } ?: return null
        if (time.size < 2) return null
        val days = intent.getIntArrayExtra(EXTRA_DAYS)?.map { DayOfWeek.entries[it] }?.toSet().orEmpty()
        return Reminder(LocalTime(time[0], time[1]), days)
    }
}
