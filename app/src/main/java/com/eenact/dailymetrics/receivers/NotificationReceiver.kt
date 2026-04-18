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
import com.eenact.dailymetrics.R
import com.eenact.dailymetrics.gui.MainActivity
import com.eenact.dailymetrics.utils.NotificationScheduler
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("task_id", -1)
        val taskName = intent.getStringExtra("task_name") ?: "Task"
        val daysString = intent.getStringExtra("notification_days")

        // If specific days are set, check if today is one of them
        if (!daysString.isNullOrEmpty()) {
            val days = daysString.split(",").mapNotNull { it.toIntOrNull() }
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (today !in days) {
                // Not scheduled for today, just schedule for tomorrow and exit
                reschedule(context, taskId, taskName, daysString)
                return
            }
        }

        showNotification(context, taskId, taskName)
        reschedule(context, taskId, taskName, daysString)
    }

    private fun reschedule(context: Context, taskId: Long, taskName: String, days: String?) {
        // Logic to schedule the next occurrence (e.g., tomorrow)
        // For simplicity, we can call a simplified scheduler or just rely on the next day's check
        // In a real app, you'd calculate the exact next date/time here.
    }

    private fun showNotification(context: Context, taskId: Long, taskName: String) {
        val channelId = "task_reminders"
        createNotificationChannel(context, channelId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Using mipmap as ic_launcher_foreground might not exist
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text, taskName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(taskId.toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}