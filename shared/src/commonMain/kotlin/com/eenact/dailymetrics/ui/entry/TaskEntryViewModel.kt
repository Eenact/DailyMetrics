package com.eenact.dailymetrics.ui.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eenact.dailymetrics.data.TaskRepository
import com.eenact.dailymetrics.domain.model.EntryType
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.domain.toLocalDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock

enum class SaveResult { Saved, MissingTask, MissingValue, DuplicateTime }

class TaskEntryViewModel(
    private val repository: TaskRepository,
    clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    val tasks: StateFlow<List<Task>> =
        repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var selectedTask by mutableStateOf<Task?>(null)
        private set

    /** true = Yes, false = No, null = not chosen yet. */
    var yesNo by mutableStateOf<Boolean?>(null)
    var numberText by mutableStateOf("")
    var comment by mutableStateOf("")
    var valueError by mutableStateOf(false)

    /** Event time in epoch millis. Starts at "now"; picking a date or time zeroes the seconds. */
    var eventTime by mutableStateOf(clock.now().toEpochMilliseconds())
        private set

    val eventDate: LocalDate get() = eventTime.toLocalDateTime(zone).date
    val eventClock: LocalTime get() = eventTime.toLocalDateTime(zone).time

    fun selectTask(task: Task) {
        selectedTask = task
        valueError = false
    }

    fun setDate(date: LocalDate) {
        eventTime = LocalDateTime(date, LocalTime(eventClock.hour, eventClock.minute)).toInstant(zone).toEpochMilliseconds()
    }

    fun setTime(time: LocalTime) {
        eventTime = LocalDateTime(eventDate, LocalTime(time.hour, time.minute)).toInstant(zone).toEpochMilliseconds()
    }

    suspend fun save(): SaveResult {
        val task = selectedTask ?: return SaveResult.MissingTask
        val value = when (task.type) {
            EntryType.YesNo -> yesNo?.let { if (it) "1" else "0" }
            EntryType.Number -> numberText.trim().replace(',', '.').toFloatOrNull()?.toString()
        }
        if (value == null) {
            valueError = true
            return SaveResult.MissingValue
        }
        val added = repository.addRecord(task.id, eventTime, value, comment.trim())
        return if (added) SaveResult.Saved else SaveResult.DuplicateTime
    }
}
