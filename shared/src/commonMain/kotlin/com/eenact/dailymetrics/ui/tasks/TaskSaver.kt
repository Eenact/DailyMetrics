package com.eenact.dailymetrics.ui.tasks

import com.eenact.dailymetrics.data.TaskRepository
import com.eenact.dailymetrics.platform.NotificationScheduler
import com.eenact.dailymetrics.ui.components.TaskFormState

enum class TaskSaveResult { Saved, NameRequired, NameExists }

/** Validates and persists a [TaskFormState], then (re)schedules its reminder. */
class TaskSaver(
    private val repository: TaskRepository,
    private val notificationScheduler: NotificationScheduler,
) {
    suspend fun save(form: TaskFormState): TaskSaveResult {
        val task = form.toTask()
        if (task.name.isEmpty()) return TaskSaveResult.NameRequired

        val nameChanged = task.name != form.originalName
        if (nameChanged && repository.taskNameExists(task.name)) return TaskSaveResult.NameExists

        val saved = if (form.isEditing) {
            if (!repository.updateTask(task)) return TaskSaveResult.NameExists
            task
        } else {
            task.copy(id = repository.addTask(task))
        }
        notificationScheduler.schedule(saved)
        return TaskSaveResult.Saved
    }

    /** Turns the reminder on only if the OS grants notification permission. */
    suspend fun toggleReminder(form: TaskFormState, enabled: Boolean): Boolean {
        if (!enabled) {
            form.reminderEnabled = false
            return true
        }
        val granted = notificationScheduler.requestPermission()
        form.reminderEnabled = granted
        return granted
    }
}
