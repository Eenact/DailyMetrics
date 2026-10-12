package com.eenact.dailymetrics.ui.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eenact.dailymetrics.data.TaskRepository
import com.eenact.dailymetrics.di.LocalAppContainer
import com.eenact.dailymetrics.platform.is24HourFormat
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.add
import com.eenact.dailymetrics.resources.back
import com.eenact.dailymetrics.resources.error_already_exists
import com.eenact.dailymetrics.resources.error_field_required
import com.eenact.dailymetrics.resources.ic_arrow_back
import com.eenact.dailymetrics.resources.task_notification_permission_denied
import com.eenact.dailymetrics.resources.task_title
import com.eenact.dailymetrics.resources.update
import com.eenact.dailymetrics.ui.components.TaskForm
import com.eenact.dailymetrics.ui.PreviewData
import com.eenact.dailymetrics.ui.components.TaskFormState
import com.eenact.dailymetrics.ui.theme.DailyMetricsTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class TaskEditViewModel(
    private val taskId: Long?,
    private val repository: TaskRepository,
    val saver: TaskSaver,
) : ViewModel() {

    var form by mutableStateOf<TaskFormState?>(if (taskId == null) TaskFormState() else null)
        private set

    init {
        if (taskId != null) {
            viewModelScope.launch { form = TaskFormState(repository.getTask(taskId)) }
        }
    }
}

@Composable
fun TaskEditScreen(taskId: Long?, onBack: () -> Unit, onSaved: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = viewModel(key = "task-edit-$taskId") {
        TaskEditViewModel(taskId, container.repository, TaskSaver(container.repository, container.notificationScheduler))
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    TaskEditContent(
        form = viewModel.form,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onReminderToggled = { form, enabled ->
            scope.launch {
                if (!viewModel.saver.toggleReminder(form, enabled)) {
                    snackbarHostState.showSnackbar(getString(Res.string.task_notification_permission_denied))
                }
            }
        },
        onSave = { form ->
            scope.launch {
                when (viewModel.saver.save(form)) {
                    TaskSaveResult.Saved -> onSaved()
                    TaskSaveResult.NameRequired -> form.nameError = getString(Res.string.error_field_required)
                    TaskSaveResult.NameExists -> form.nameError = getString(Res.string.error_already_exists)
                }
            }
        },
    )
}

/** Stateless task edit UI, separate from the view model so it can be previewed. [form] is null while loading. */
@Composable
private fun TaskEditContent(
    form: TaskFormState?,
    onBack: () -> Unit,
    onReminderToggled: (TaskFormState, Boolean) -> Unit,
    onSave: (TaskFormState) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.task_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(painterResource(Res.drawable.ic_arrow_back), stringResource(Res.string.back)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (form == null) return@Scaffold
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 16.dp),
        ) {
            TaskForm(
                state = form,
                is24Hour = is24HourFormat(),
                onReminderToggled = { enabled -> onReminderToggled(form, enabled) },
                modifier = Modifier.fillMaxWidth(),
            )
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Button(onClick = { onSave(form) }) {
                    Text(stringResource(if (form.isEditing) Res.string.update else Res.string.add))
                }
            }
        }
    }
}

@Preview
@Composable
private fun TaskEditScreenPreview() {
    DailyMetricsTheme {
        TaskEditContent(
            form = remember { TaskFormState(PreviewData.workout) },
            onBack = {},
            onReminderToggled = { _, _ -> },
            onSave = {},
        )
    }
}
