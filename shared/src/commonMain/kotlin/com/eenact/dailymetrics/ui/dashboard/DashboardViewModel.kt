package com.eenact.dailymetrics.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eenact.dailymetrics.data.TaskRepository
import com.eenact.dailymetrics.domain.ChartDataBuilder
import com.eenact.dailymetrics.domain.CsvExporter
import com.eenact.dailymetrics.domain.CsvFile
import com.eenact.dailymetrics.domain.DashboardSnapshot
import com.eenact.dailymetrics.domain.InsightsGenerator
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.domain.model.TaskRecord
import com.eenact.dailymetrics.domain.model.TimeRange
import com.eenact.dailymetrics.platform.FileExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

enum class ExportAction { Share, Save }

data class DashboardUiState(
    val isLoading: Boolean = true,
    val range: TimeRange = TimeRange.Week,
    val tasks: List<Task> = emptyList(),
    val allRecords: List<TaskRecord> = emptyList(),
    /** Tasks switched off in the filter sheet. Tasks are visible by default, including new ones. */
    val hiddenTaskIds: Set<Long> = emptySet(),
    val snapshot: DashboardSnapshot? = null,
    val insights: List<String>? = null,
) {
    val visibleTaskIds: Set<Long> get() = tasks.map { it.id }.toSet() - hiddenTaskIds
}

class DashboardViewModel(
    private val repository: TaskRepository,
    private val fileExporter: FileExporter,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val range = MutableStateFlow(TimeRange.Week)
    private val hiddenTaskIds = MutableStateFlow(emptySet<Long>())
    private val insights = MutableStateFlow<List<String>?>(null)

    val state: StateFlow<DashboardUiState> = combine(
        repository.tasks, repository.records, range, hiddenTaskIds, insights,
    ) { tasks, records, range, hidden, insights ->
        val visible = tasks.map { it.id }.toSet() - hidden
        DashboardUiState(
            isLoading = false,
            range = range,
            tasks = tasks,
            allRecords = records,
            hiddenTaskIds = hidden,
            snapshot = ChartDataBuilder.build(records, visible, range, clock.now(), zone),
            insights = insights,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun selectRange(newRange: TimeRange) = range.update { newRange }

    fun applyFilter(visibleTaskIds: Set<Long>) {
        hiddenTaskIds.value = state.value.tasks.map { it.id }.toSet() - visibleTaskIds
    }

    fun deleteRecord(record: TaskRecord) {
        viewModelScope.launch { repository.deleteRecord(record) }
    }

    fun generateInsights() {
        val current = state.value
        val visible = current.visibleTaskIds
        insights.value = InsightsGenerator.generate(
            records = current.allRecords.filter { it.task.id in visible },
            nowMillis = clock.now().toEpochMilliseconds(),
        )
    }

    /** Exports all (or only the filtered) records. Returns true when a saved file was written. */
    suspend fun export(action: ExportAction, onlyFiltered: Boolean): Boolean {
        val current = state.value
        val records = if (onlyFiltered) {
            current.allRecords.filter { it.task.id in current.visibleTaskIds }
        } else {
            current.allRecords
        }
        val file = CsvExporter.export(records, onlyFiltered, clock.now().toEpochMilliseconds(), zone)
        return when (action) {
            ExportAction.Share -> {
                fileExporter.share(file.fileName, CsvFile.MIME_TYPE, file.content)
                false
            }
            ExportAction.Save -> fileExporter.save(file.fileName, CsvFile.MIME_TYPE, file.content)
        }
    }
}
