package com.eenact.dailymetrics.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eenact.dailymetrics.db.AppDatabase
import com.eenact.dailymetrics.db.TaskEntity
import com.eenact.dailymetrics.utils.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.taskEntitiesDao()
            
            CoroutineScope(Dispatchers.IO).launch {
                val tasks = dao.getAllTasksSync()
                tasks.forEach { task: TaskEntity ->
                    if (task.isNotificationEnabled) {
                        NotificationScheduler.scheduleNotification(context, task)
                    }
                }
            }
        }
    }
}