package com.eenact.dailymetrics.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eenact.dailymetrics.data.TaskRepository
import com.eenact.dailymetrics.di.LocalAppContainer
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.platform.NotificationScheduler
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.back
import com.eenact.dailymetrics.resources.content_desc_insert_task_btn
import com.eenact.dailymetrics.resources.delete
import com.eenact.dailymetrics.resources.ic_add
import com.eenact.dailymetrics.resources.ic_arrow_back
import com.eenact.dailymetrics.resources.item_deleted
import com.eenact.dailymetrics.resources.task_manager_title
import com.eenact.dailymetrics.resources.task_no_item
import com.eenact.dailymetrics.ui.PreviewData
import com.eenact.dailymetrics.ui.theme.DailyMetricsTheme
import com.eenact.dailymetrics.ui.theme.SecondaryText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class TaskManagerViewModel(
    private val repository: TaskRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    /** Null while loading. */
    val tasks: StateFlow<List<Task>?> = repository.tasks
        .map<List<Task>, List<Task>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(task: Task) {
        notificationScheduler.cancel(task.id)
        viewModelScope.launch { repository.deleteTask(task) }
    }
}

@Composable
fun TaskManagerScreen(onBack: () -> Unit, onAddTask: () -> Unit, onEditTask: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = viewModel { TaskManagerViewModel(container.repository, container.notificationScheduler) }
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    TaskManagerContent(
        tasks = tasks,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onAddTask = onAddTask,
        onEditTask = onEditTask,
        onDeleteTask = { task ->
            viewModel.delete(task)
            scope.launch { snackbarHostState.showSnackbar(getString(Res.string.item_deleted, task.name)) }
        },
    )
}

/** Stateless task list UI, separate from the view model so it can be previewed. [tasks] is null while loading. */
@Composable
private fun TaskManagerContent(
    tasks: List<Task>?,
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onDeleteTask: (Task) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.task_manager_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(painterResource(Res.drawable.ic_arrow_back), stringResource(Res.string.back)) }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(painterResource(Res.drawable.ic_add), stringResource(Res.string.content_desc_insert_task_btn), Modifier.size(24.dp))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val list = tasks ?: return@Scaffold
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.task_no_item), color = SecondaryText, textAlign = TextAlign.Center)
            }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            items(list, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    onClick = { onEditTask(task.id) },
                    onDelete = { onDeleteTask(task) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun TaskManagerScreenPreview() {
    DailyMetricsTheme {
        TaskManagerContent(tasks = PreviewData.tasks, onBack = {}, onAddTask = {}, onEditTask = {}, onDeleteTask = {})
    }
}

@Composable
private fun TaskRow(task: Task, onClick: () -> Unit, onDelete: () -> Unit) {
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
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).clickable(onClick = onClick)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(task.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(task.type.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                task.displayUnit?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(task.color))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                )
            }
            HorizontalDivider()
        }
    }
}
