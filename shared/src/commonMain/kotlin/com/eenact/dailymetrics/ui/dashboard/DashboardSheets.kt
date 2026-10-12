package com.eenact.dailymetrics.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eenact.dailymetrics.domain.model.Task
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.apply
import com.eenact.dailymetrics.resources.cancel
import com.eenact.dailymetrics.resources.dashboard_filter_select_all
import com.eenact.dailymetrics.resources.dashboard_filter_title
import com.eenact.dailymetrics.resources.export_data_list
import com.eenact.dailymetrics.resources.export_data_title
import com.eenact.dailymetrics.resources.export_filtered
import com.eenact.dailymetrics.resources.export_share_list
import com.eenact.dailymetrics.resources.ic_save
import com.eenact.dailymetrics.resources.ic_share
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterSheet(
    tasks: List<Task>,
    visibleTaskIds: Set<Long>,
    onDismiss: () -> Unit,
    onApply: (Set<Long>) -> Unit,
) {
    var selected by remember { mutableStateOf(visibleTaskIds) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.dashboard_filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                CheckRow(
                    label = stringResource(Res.string.dashboard_filter_select_all),
                    checked = selected.size == tasks.size,
                    onCheckedChange = { all -> selected = if (all) tasks.map { it.id }.toSet() else emptySet() },
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            tasks.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth()) {
                    pair.forEach { task ->
                        CheckRow(
                            label = task.name,
                            checked = task.id in selected,
                            onCheckedChange = { checked -> selected = if (checked) selected + task.id else selected - task.id },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
                Spacer(Modifier.width(12.dp))
                Button(onClick = { onApply(selected) }) { Text(stringResource(Res.string.apply)) }
            }
        }
    }
}

@Composable
fun ExportSheet(
    onDismiss: () -> Unit,
    onExport: (action: ExportAction, onlyFiltered: Boolean) -> Unit,
) {
    var onlyFiltered by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.export_data_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                CheckRow(
                    label = stringResource(Res.string.export_filtered),
                    checked = onlyFiltered,
                    onCheckedChange = { onlyFiltered = it },
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            ExportRow(Res.drawable.ic_share, stringResource(Res.string.export_share_list)) {
                onExport(ExportAction.Share, onlyFiltered)
            }
            HorizontalDivider()
            ExportRow(Res.drawable.ic_save, stringResource(Res.string.export_data_list)) {
                onExport(ExportAction.Save, onlyFiltered)
            }
        }
    }
}

@Composable
private fun ExportRow(icon: DrawableResource, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CheckRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
