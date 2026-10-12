package com.eenact.dailymetrics.data

import com.eenact.dailymetrics.data.db.AppDatabase
import com.eenact.dailymetrics.data.db.TaskEntity
import com.eenact.dailymetrics.data.db.TaskValueEntity
import com.eenact.dailymetrics.domain.model.Reminder
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.domain.model.TaskRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber

/**
 * Single source of truth for tasks and their logged values.
 *
 * UI and view models depend on this interface only, so a future target without Room support can
 * provide its own implementation.
 */
interface TaskRepository {
    val tasks: Flow<List<Task>>
    val records: Flow<List<TaskRecord>>

    suspend fun getTasks(): List<Task>
    suspend fun getTask(id: Long): Task?
    suspend fun taskNameExists(name: String): Boolean

    /** Returns the id of the new task. */
    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task): Boolean
    suspend fun deleteTask(task: Task)

    /** Returns false when a record already exists at the same timestamp. */
    suspend fun addRecord(taskId: Long, time: Long, value: String, comment: String): Boolean
    suspend fun deleteRecord(record: TaskRecord)
}

class RoomTaskRepository(private val database: AppDatabase) : TaskRepository {

    private val taskDao get() = database.taskDao()
    private val valueDao get() = database.taskValueDao()

    override val tasks: Flow<List<Task>> = taskDao.observeAll().map { list -> list.map { it.toDomain() } }

    override val records: Flow<List<TaskRecord>> = valueDao.observeAllWithTask().map { rows ->
        rows.map { row ->
            TaskRecord(
                task = row.task.toDomain(),
                time = row.value.time,
                value = row.value.taskValue,
                comment = row.value.comments,
            )
        }
    }

    override suspend fun getTasks(): List<Task> = taskDao.getAll().map { it.toDomain() }

    override suspend fun getTask(id: Long): Task? = taskDao.getById(id)?.toDomain()

    override suspend fun taskNameExists(name: String): Boolean = taskDao.exists(name)

    override suspend fun addTask(task: Task): Long = taskDao.insert(task.toEntity().copy(id = 0))

    override suspend fun updateTask(task: Task): Boolean = taskDao.update(task.toEntity()) == 1

    override suspend fun deleteTask(task: Task) = taskDao.delete(task.toEntity())

    override suspend fun addRecord(taskId: Long, time: Long, value: String, comment: String): Boolean =
        valueDao.insert(TaskValueEntity(time = time, taskId = taskId, taskValue = value, comments = comment)) != -1L

    override suspend fun deleteRecord(record: TaskRecord) = valueDao.deleteByTime(record.time)
}

private const val DEFAULT_REMINDER_TIME = "09:00"

internal fun TaskEntity.toDomain(): Task = Task(
    id = id,
    name = name,
    type = type,
    unit = unit,
    color = color,
    reminder = if (isNotificationEnabled) {
        Reminder(
            time = parseTime(notificationTime ?: DEFAULT_REMINDER_TIME),
            days = parseDays(notificationDays),
        )
    } else {
        null
    },
)

internal fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    name = name,
    type = type,
    unit = unit,
    color = color,
    isNotificationEnabled = reminder != null,
    notificationTime = reminder?.time?.let { formatTime(it) },
    notificationDays = reminder?.days?.let { formatDays(it) },
)

private fun parseTime(value: String): LocalTime {
    val parts = value.split(":").mapNotNull { it.trim().toIntOrNull() }
    return if (parts.size >= 2 && parts[0] in 0..23 && parts[1] in 0..59) LocalTime(parts[0], parts[1]) else LocalTime(9, 0)
}

private fun formatTime(time: LocalTime): String =
    "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

// The original app stored java.util.Calendar day numbers: Sunday = 1, Monday = 2 … Saturday = 7.

internal fun DayOfWeek.toCalendarDay(): Int = isoDayNumber % 7 + 1

internal fun calendarDayToDayOfWeek(day: Int): DayOfWeek? = when (day) {
    1 -> DayOfWeek.SUNDAY
    in 2..7 -> DayOfWeek.entries.first { it.isoDayNumber == day - 1 }
    else -> null
}

internal fun parseDays(value: String?): Set<DayOfWeek> =
    value.orEmpty().split(",").mapNotNull { it.trim().toIntOrNull()?.let(::calendarDayToDayOfWeek) }.toSet()

internal fun formatDays(days: Set<DayOfWeek>): String =
    days.sortedBy { it.isoDayNumber }.joinToString(",") { it.toCalendarDay().toString() }
