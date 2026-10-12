package com.eenact.dailymetrics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.eenact.dailymetrics.domain.DateTimeFormatting
import com.eenact.dailymetrics.domain.model.EntryType
import com.eenact.dailymetrics.domain.model.MeasureUnit
import com.eenact.dailymetrics.domain.model.Reminder
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.entry_time_picker_dialog_title
import com.eenact.dailymetrics.resources.task_details_color
import com.eenact.dailymetrics.resources.task_details_type
import com.eenact.dailymetrics.resources.task_details_unit
import com.eenact.dailymetrics.resources.task_header_color
import com.eenact.dailymetrics.resources.task_header_name
import com.eenact.dailymetrics.resources.task_header_type
import com.eenact.dailymetrics.resources.task_header_unit
import com.eenact.dailymetrics.resources.task_notification_days
import com.eenact.dailymetrics.resources.task_notification_enable
import com.eenact.dailymetrics.resources.task_notification_set_time
import com.eenact.dailymetrics.resources.task_notification_time
import com.eenact.dailymetrics.ui.theme.DefaultLegendColor
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

private val DefaultReminderTime = LocalTime(9, 0)

/** Editable state of a task, shared by onboarding and the task create/edit screen. */
class TaskFormState(initial: Task? = null) {
    val editingTaskId: Long? = initial?.id
    val originalName: String? = initial?.name

    var name by mutableStateOf(initial?.name.orEmpty())
    var nameError by mutableStateOf<String?>(null)
    var type by mutableStateOf(initial?.type ?: EntryType.YesNo)

    /** Null until the user picks one; saved as [MeasureUnit.Other]. */
    var unit by mutableStateOf(initial?.unit?.takeIf { initial.type == EntryType.Number })
    var color by mutableStateOf(initial?.color ?: DefaultLegendColor)
    var reminderEnabled by mutableStateOf(initial?.reminder != null)
    var reminderTime by mutableStateOf(initial?.reminder?.time)
    var reminderDays by mutableStateOf(initial?.reminder?.days.orEmpty())

    val isEditing: Boolean get() = editingTaskId != null

    fun toTask(): Task = Task(
        id = editingTaskId ?: 0,
        name = name.trim(),
        type = type,
        unit = if (type == EntryType.Number) unit ?: MeasureUnit.Other else MeasureUnit.Other,
        color = color,
        reminder = if (reminderEnabled) Reminder(reminderTime ?: DefaultReminderTime, reminderDays) else null,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskForm(
    state: TaskFormState,
    onReminderToggled: (Boolean) -> Unit,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.name,
            onValueChange = {
                state.name = it
                state.nameError = null
            },
            label = { Text(stringResource(Res.string.task_header_name)) },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        DropdownField(
            label = stringResource(Res.string.task_header_type),
            selected = state.type,
            options = EntryType.entries,
            optionLabel = { it.label },
            onSelected = { state.type = it },
            enabled = !state.isEditing,
            supportingText = stringResource(Res.string.task_details_type),
            modifier = Modifier.fillMaxWidth(),
        )

        DropdownField(
            label = stringResource(Res.string.task_header_unit),
            selected = state.unit.takeIf { state.type == EntryType.Number },
            options = MeasureUnit.entries,
            optionLabel = { it.label },
            onSelected = { state.unit = it },
            enabled = state.type == EntryType.Number,
            supportingText = stringResource(Res.string.task_details_unit),
            modifier = Modifier.fillMaxWidth(),
        )

        Column {
            OutlinedButton(
                onClick = { showColorPicker = true },
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(
                    stringResource(Res.string.task_header_color),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Box(Modifier.size(20.dp).background(Color(state.color)))
            }
            Text(
                stringResource(Res.string.task_details_color),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }

        HorizontalDivider(Modifier.padding(top = 12.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.task_notification_enable), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = state.reminderEnabled, onCheckedChange = onReminderToggled)
        }

        if (state.reminderEnabled) {
            OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    state.reminderTime?.let { stringResource(Res.string.task_notification_time, DateTimeFormatting.time24(it)) }
                        ?: stringResource(Res.string.task_notification_set_time),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Text(stringResource(Res.string.task_notification_days), style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DayOfWeek.entries.forEach { day ->
                    val selected = day in state.reminderDays
                    FilterChip(
                        selected = selected,
                        onClick = { state.reminderDays = if (selected) state.reminderDays - day else state.reminderDays + day },
                        label = { Text(day.shortLabel()) },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = state.color,
            onDismiss = { showColorPicker = false },
            onColorPicked = {
                state.color = it
                showColorPicker = false
            },
        )
    }
    if (showTimePicker) {
        TimePickerDialog(
            title = stringResource(Res.string.entry_time_picker_dialog_title),
            initial = state.reminderTime ?: DefaultReminderTime,
            is24Hour = is24Hour,
            onDismiss = { showTimePicker = false },
            onTimePicked = {
                state.reminderTime = it
                showTimePicker = false
            },
        )
    }
}

private fun DayOfWeek.shortLabel(): String = name.take(3).lowercase().replaceFirstChar { it.uppercase() }
