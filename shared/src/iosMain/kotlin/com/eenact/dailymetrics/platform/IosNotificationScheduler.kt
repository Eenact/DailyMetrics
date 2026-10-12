package com.eenact.dailymetrics.platform

import com.eenact.dailymetrics.data.toCalendarDay
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.notification_text
import com.eenact.dailymetrics.resources.notification_title
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * Uses repeating calendar triggers: one per selected weekday, or a single daily trigger when no
 * day is selected. iOS keeps them across reboots, so no boot handling is needed.
 */
class IosNotificationScheduler : NotificationScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()
    private val scope = MainScope()

    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { granted, _ ->
            continuation.resume(granted)
        }
    }

    override fun schedule(task: Task) {
        cancel(task.id)
        val reminder = task.reminder ?: return
        scope.launch {
            val content = UNMutableNotificationContent().apply {
                title = getString(Res.string.notification_title)
                body = getString(Res.string.notification_text, task.name)
                sound = UNNotificationSound.defaultSound
            }
            // NSCalendar weekdays match java.util.Calendar: Sunday = 1 … Saturday = 7.
            val weekdays: List<Int?> = reminder.days.map { it.toCalendarDay() }.ifEmpty { listOf(null) }
            weekdays.forEach { weekday ->
                val components = NSDateComponents().apply {
                    hour = reminder.time.hour.toLong()
                    minute = reminder.time.minute.toLong()
                    if (weekday != null) this.weekday = weekday.toLong()
                }
                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = requestId(task.id, weekday),
                    content = content,
                    trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = true),
                )
                center.addNotificationRequest(request, withCompletionHandler = null)
            }
        }
    }

    override fun cancel(taskId: Long) {
        val ids = (listOf<Int?>(null) + (1..7)).map { requestId(taskId, it) }
        center.removePendingNotificationRequestsWithIdentifiers(ids)
    }

    private fun requestId(taskId: Long, weekday: Int?): String = "task-$taskId-${weekday ?: "daily"}"
}
