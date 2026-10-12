package com.eenact.dailymetrics.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/** Read-only dropdown, the Compose counterpart of Material's ExposedDropdownMenu. */
@Composable
fun <T> DropdownField(
    label: String,
    selected: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false,
    supportingText: String? = null,
    errorText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        val fieldModifier = Modifier
            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
            .fillMaxWidth()
        val text = selected?.let(optionLabel).orEmpty()
        val trailing: @Composable () -> Unit = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        val supporting: (@Composable () -> Unit)? = (errorText ?: supportingText)?.let { { Text(it) } }
        if (filled) {
            TextField(
                value = text, onValueChange = {}, readOnly = true, enabled = enabled, singleLine = true,
                label = { Text(label) }, trailingIcon = trailing, supportingText = supporting,
                isError = errorText != null, modifier = fieldModifier,
            )
        } else {
            OutlinedTextField(
                value = text, onValueChange = {}, readOnly = true, enabled = enabled, singleLine = true,
                label = { Text(label) }, trailingIcon = trailing, supportingText = supporting,
                isError = errorText != null, modifier = fieldModifier,
            )
        }
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/** A read-only outlined field that behaves like a button (used for date and time pickers). */
@Composable
fun ClickableField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource, enabled) {
        interactionSource.interactions.collect { if (enabled && it is PressInteraction.Release) onClick() }
    }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        singleLine = true,
        label = { Text(label) },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
        interactionSource = interactionSource,
        modifier = modifier,
    )
}
