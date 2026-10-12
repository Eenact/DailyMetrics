package com.eenact.dailymetrics.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eenact.dailymetrics.di.LocalAppContainer
import com.eenact.dailymetrics.domain.DateTimeFormatting
import com.eenact.dailymetrics.domain.model.EntryType
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.platform.is24HourFormat
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.close
import com.eenact.dailymetrics.resources.entry_comment_title
import com.eenact.dailymetrics.resources.entry_date_title
import com.eenact.dailymetrics.resources.entry_duplicate_time
import com.eenact.dailymetrics.resources.entry_event_date_time_title
import com.eenact.dailymetrics.resources.entry_no
import com.eenact.dailymetrics.resources.entry_number_title
import com.eenact.dailymetrics.resources.entry_time_picker_dialog_title
import com.eenact.dailymetrics.resources.entry_time_title
import com.eenact.dailymetrics.resources.entry_yes
import com.eenact.dailymetrics.resources.entry_yesno_title
import com.eenact.dailymetrics.resources.error_field_required
import com.eenact.dailymetrics.resources.error_invalid_field_name
import com.eenact.dailymetrics.resources.ic_close
import com.eenact.dailymetrics.resources.save
import com.eenact.dailymetrics.resources.task_entry_title
import com.eenact.dailymetrics.resources.task_header_name
import com.eenact.dailymetrics.ui.PreviewData
import com.eenact.dailymetrics.ui.components.ClickableField
import com.eenact.dailymetrics.ui.components.DropdownField
import com.eenact.dailymetrics.ui.components.PastDatePickerDialog
import com.eenact.dailymetrics.ui.components.TimePickerDialog
import com.eenact.dailymetrics.ui.theme.DailyMetricsTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaskEntryScreen(onClose: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = viewModel { TaskEntryViewModel(container.repository) }
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    TaskEntryContent(
        tasks = tasks,
        task = viewModel.selectedTask,
        yesNo = viewModel.yesNo,
        numberText = viewModel.numberText,
        comment = viewModel.comment,
        valueError = viewModel.valueError,
        eventDate = viewModel.eventDate,
        eventClock = viewModel.eventClock,
        snackbarHostState = snackbarHostState,
        onClose = onClose,
        onSave = {
            scope.launch {
                when (viewModel.save()) {
                    SaveResult.Saved -> onClose()
                    SaveResult.MissingTask -> snackbarHostState.showSnackbar(getString(Res.string.error_invalid_field_name))
                    SaveResult.DuplicateTime -> snackbarHostState.showSnackbar(getString(Res.string.entry_duplicate_time))
                    SaveResult.MissingValue -> Unit // shown inline
                }
            }
        },
        onTaskSelected = viewModel::selectTask,
        onYesNoChanged = {
            viewModel.yesNo = it
            viewModel.valueError = false
        },
        onNumberTextChanged = {
            viewModel.numberText = it
            viewModel.valueError = false
        },
        onCommentChanged = { viewModel.comment = it },
        onDateClick = { showDatePicker = true },
        onTimeClick = { showTimePicker = true },
    )

    if (showDatePicker) {
        PastDatePickerDialog(
            initial = viewModel.eventDate,
            onDismiss = { showDatePicker = false },
            onDatePicked = {
                viewModel.setDate(it)
                showDatePicker = false
            },
        )
    }
    if (showTimePicker) {
        TimePickerDialog(
            title = stringResource(Res.string.entry_time_picker_dialog_title),
            initial = viewModel.eventClock,
            is24Hour = is24HourFormat(),
            onDismiss = { showTimePicker = false },
            onTimePicked = {
                viewModel.setTime(it)
                showTimePicker = false
            },
        )
    }
}

/** Stateless value entry UI, separate from the view model so it can be previewed. */
@Composable
private fun TaskEntryContent(
    tasks: List<Task>,
    task: Task?,
    yesNo: Boolean?,
    numberText: String,
    comment: String,
    valueError: Boolean,
    eventDate: LocalDate,
    eventClock: LocalTime,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onTaskSelected: (Task) -> Unit,
    onYesNoChanged: (Boolean) -> Unit,
    onNumberTextChanged: (String) -> Unit,
    onCommentChanged: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val yes = stringResource(Res.string.entry_yes)
    val no = stringResource(Res.string.entry_no)
    val requiredError = stringResource(Res.string.error_field_required)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.task_entry_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(painterResource(Res.drawable.ic_close), stringResource(Res.string.close)) }
                },
                actions = {
                    TextButton(onClick = onSave) { Text(stringResource(Res.string.save)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DropdownField(
                label = stringResource(Res.string.task_header_name),
                selected = task,
                options = tasks,
                optionLabel = { it.name },
                onSelected = onTaskSelected,
                filled = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (task?.type == EntryType.Number) {
                OutlinedTextField(
                    value = numberText,
                    onValueChange = onNumberTextChanged,
                    label = { Text(task.displayUnit ?: stringResource(Res.string.entry_number_title)) },
                    isError = valueError,
                    supportingText = if (valueError) ({ Text(requiredError) }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                DropdownField(
                    label = stringResource(Res.string.entry_yesno_title),
                    selected = yesNo,
                    options = listOf(true, false),
                    optionLabel = { if (it) yes else no },
                    onSelected = onYesNoChanged,
                    enabled = task != null,
                    errorText = if (valueError) requiredError else null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text(
                stringResource(Res.string.entry_event_date_time_title),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ClickableField(
                    label = stringResource(Res.string.entry_date_title),
                    value = DateTimeFormatting.date(eventDate),
                    onClick = onDateClick,
                    enabled = task != null,
                    modifier = Modifier.weight(1f),
                )
                ClickableField(
                    label = stringResource(Res.string.entry_time_title),
                    value = DateTimeFormatting.time(eventClock),
                    onClick = onTimeClick,
                    enabled = task != null,
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChanged,
                label = { Text(stringResource(Res.string.entry_comment_title)) },
                enabled = task != null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun TaskEntryScreenPreview() {
    DailyMetricsTheme {
        TaskEntryContent(
            tasks = PreviewData.tasks,
            task = PreviewData.workout,
            yesNo = null,
            numberText = "45",
            comment = "Evening run",
            valueError = false,
            eventDate = LocalDate(2026, 10, 4),
            eventClock = LocalTime(18, 30),
            onClose = {},
            onSave = {},
            onTaskSelected = {},
            onYesNoChanged = {},
            onNumberTextChanged = {},
            onCommentChanged = {},
            onDateClick = {},
            onTimeClick = {},
        )
    }
}
