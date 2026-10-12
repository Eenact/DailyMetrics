package com.eenact.dailymetrics.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eenact.dailymetrics.chart.LineChart
import com.eenact.dailymetrics.di.LocalAppContainer
import com.eenact.dailymetrics.domain.DateTimeFormatting
import com.eenact.dailymetrics.domain.model.TaskRecord
import com.eenact.dailymetrics.domain.model.TimeRange
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.content_desc_filter_btn
import com.eenact.dailymetrics.resources.content_desc_insert_value_btn
import com.eenact.dailymetrics.resources.dashboard_no_chart_data
import com.eenact.dailymetrics.resources.dashboard_no_entry
import com.eenact.dailymetrics.resources.dashboard_no_task
import com.eenact.dailymetrics.resources.dashboard_title
import com.eenact.dailymetrics.resources.delete
import com.eenact.dailymetrics.resources.deleted_value
import com.eenact.dailymetrics.resources.export_data_title
import com.eenact.dailymetrics.resources.export_progress_completed
import com.eenact.dailymetrics.resources.ic_add
import com.eenact.dailymetrics.resources.ic_filter
import com.eenact.dailymetrics.resources.ic_more_vert
import com.eenact.dailymetrics.resources.insights_button
import com.eenact.dailymetrics.resources.insights_collecting
import com.eenact.dailymetrics.resources.insights_title
import com.eenact.dailymetrics.resources.item_deleted
import com.eenact.dailymetrics.resources.legal_title
import com.eenact.dailymetrics.resources.more_options
import com.eenact.dailymetrics.resources.task_manager_title
import com.eenact.dailymetrics.ui.PreviewData
import com.eenact.dailymetrics.ui.theme.DailyMetricsTheme
import com.eenact.dailymetrics.ui.theme.SecondaryText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardScreen(
    onAddValue: () -> Unit,
    onManageTasks: () -> Unit,
    onLegalTerms: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel = viewModel { DashboardViewModel(container.repository, container.fileExporter) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilter by remember { mutableStateOf(false) }
    var showExport by remember { mutableStateOf(false) }

    fun snack(message: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(message, actionLabel, duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) onAction()
        }
    }

    fun noTaskSnack() = scope.launch {
        snack(getString(Res.string.dashboard_no_task), getString(Res.string.task_manager_title), onManageTasks)
    }

    // Same nudge as the original app when there is nothing to show yet.
    LaunchedEffect(state.isLoading, state.tasks.isEmpty()) {
        if (!state.isLoading && state.tasks.isEmpty()) noTaskSnack()
    }

    DashboardContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onRangeSelected = viewModel::selectRange,
        onAddValueClick = { if (state.tasks.isNotEmpty()) onAddValue() else noTaskSnack() },
        onFilterClick = { if (state.tasks.isEmpty()) noTaskSnack() else showFilter = true },
        onManageTasks = onManageTasks,
        onExportClick = {
            if (state.allRecords.isEmpty()) {
                scope.launch {
                    snack(
                        getString(Res.string.dashboard_no_entry),
                        getString(Res.string.content_desc_insert_value_btn),
                        onAddValue,
                    )
                }
            } else {
                showExport = true
            }
        },
        onLegalTerms = onLegalTerms,
        onGetInsights = viewModel::generateInsights,
        onDeleteRecord = { record ->
            viewModel.deleteRecord(record)
            scope.launch { snack(getString(Res.string.item_deleted, getString(Res.string.deleted_value))) }
        },
    )

    if (showFilter) {
        FilterSheet(
            tasks = state.tasks,
            visibleTaskIds = state.visibleTaskIds,
            onDismiss = { showFilter = false },
            onApply = {
                viewModel.applyFilter(it)
                showFilter = false
            },
        )
    }
    if (showExport) {
        ExportSheet(
            onDismiss = { showExport = false },
            onExport = { action, onlyFiltered ->
                showExport = false
                scope.launch {
                    if (viewModel.export(action, onlyFiltered)) snack(getString(Res.string.export_progress_completed))
                }
            },
        )
    }
}

/** Stateless dashboard UI, separate from the view model so it can be previewed. */
@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onRangeSelected: (TimeRange) -> Unit,
    onAddValueClick: () -> Unit,
    onFilterClick: () -> Unit,
    onManageTasks: () -> Unit,
    onExportClick: () -> Unit,
    onLegalTerms: () -> Unit,
    onGetInsights: () -> Unit,
    onDeleteRecord: (TaskRecord) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.dashboard_title)) },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(painterResource(Res.drawable.ic_more_vert), stringResource(Res.string.more_options))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.task_manager_title)) },
                                onClick = { menuExpanded = false; onManageTasks() },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.export_data_title)) },
                                onClick = { menuExpanded = false; onExportClick() },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.legal_title)) },
                                onClick = { menuExpanded = false; onLegalTerms() },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddValueClick,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(painterResource(Res.drawable.ic_add), stringResource(Res.string.content_desc_insert_value_btn), Modifier.size(24.dp))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 24.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                        TimeRange.entries.forEachIndexed { index, range ->
                            SegmentedButton(
                                selected = state.range == range,
                                onClick = { onRangeSelected(range) },
                                shape = SegmentedButtonDefaults.itemShape(index, TimeRange.entries.size),
                                icon = {},
                            ) { Text(range.label, style = MaterialTheme.typography.labelMedium) }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(onClick = onFilterClick) {
                        Icon(painterResource(Res.drawable.ic_filter), stringResource(Res.string.content_desc_filter_btn), Modifier.size(24.dp))
                    }
                }
            }

            item {
                OutlinedCard(Modifier.fillMaxWidth()) {
                    val snapshot = state.snapshot
                    if (snapshot != null) {
                        LineChart(
                            data = snapshot.chart,
                            noDataText = stringResource(Res.string.dashboard_no_chart_data),
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                        )
                    } else {
                        Spacer(Modifier.height(280.dp))
                    }
                }
            }

            items(state.snapshot?.records.orEmpty(), key = { it.time }) { record ->
                RecordRow(
                    record = record,
                    onDelete = { onDeleteRecord(record) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
    DailyMetricsTheme {
        DashboardContent(
            state = DashboardUiState(
                isLoading = false,
                tasks = PreviewData.tasks,
                allRecords = PreviewData.records,
                snapshot = PreviewData.snapshot(),
            ),
            onRangeSelected = {},
            onAddValueClick = {},
            onFilterClick = {},
            onManageTasks = {},
            onExportClick = {},
            onLegalTerms = {},
            onGetInsights = {},
            onDeleteRecord = {},
        )
    }
}

@Composable
private fun InsightsCard(insights: List<String>?, onGetInsights: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(Res.string.insights_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (insights == null) {
                Text(stringResource(Res.string.insights_collecting), style = MaterialTheme.typography.bodyLarge)
            } else {
                insights.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                OutlinedButton(onClick = onGetInsights) {
                    Text(stringResource(Res.string.insights_button), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun RecordRow(record: TaskRecord, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        onDismiss = { if (it == SwipeToDismissBoxValue.EndToStart) onDelete() },
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(Color.Red).padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) { Text(stringResource(Res.string.delete), color = Color.White) }
        },
    ) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${DateTimeFormatting.date(record.time)} ${DateTimeFormatting.time(record.time)}",
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row {
                    Text(record.task.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(record.displayValue, style = MaterialTheme.typography.bodyLarge)
                }
                if (record.comment.isNotEmpty()) {
                    Text(record.comment, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                }
            }
            HorizontalDivider()
        }
    }
}
