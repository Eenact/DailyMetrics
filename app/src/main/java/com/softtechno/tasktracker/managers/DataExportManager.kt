package com.softtechno.tasktracker.managers

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.softtechno.tasktracker.BuildConfig
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskValue
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DataExportManager(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {

    companion object {
        private const val LINE_BREAK: String = "\n"
    }

    lateinit var startForResult: ActivityResultLauncher<Intent>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        startForResult = registry.register("", ActivityResultContracts.StartActivityForResult()) {

        }
    }

    fun showExportDialog(
        context: Context, allContent: Map<TaskEntity, TaskValue>, filters: MutableMap<Long, Boolean>
    ) {

        MaterialAlertDialogBuilder(context).setTitle(R.string.export_dialog_title)
            .setMessage(R.string.export_dialog_message)
            .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.setNeutralButton(R.string.export_filtered) { _, _ ->
                val fileName =
                    createCsvFile(context, allContent.filter { filters[it.key.id] == true }, true)
                shareExportedFile(context, fileName)
            }.setPositiveButton(R.string.export_all) { _, _ ->
                val fileName = createCsvFile(context, allContent, false)
                shareExportedFile(context, fileName)
            }.show()

    }

    private fun createCsvFile(
        context: Context, taskValues: Map<TaskEntity, TaskValue>, isFiltered: Boolean
    ): String {

        val datetimeFormatter = SimpleDateFormat(
            context.getString(R.string.export_datetime_value_format), Locale.getDefault()
        )

        val csvContent = buildString {

            //include csv headers
            append(
                context.getString(
                    R.string.export_value,
                    context.getString(R.string.export_header_date),
                    context.getString(R.string.export_header_name),
                    context.getString(R.string.export_header_unit),
                    context.getString(R.string.export_header_value),
                    context.getString(R.string.export_header_comments)
                )
            ).append(LINE_BREAK)

            // include content
            append(taskValues.run {
                map {
                    context.getString(
                        R.string.export_value,
                        datetimeFormatter.format(Date(it.value.time)),
                        it.key.name,
                        it.key.unit.toString(),
                        it.value.value.toString(),
                        it.value.comments
                    )
                }.joinToString(LINE_BREAK)
            })
        }

        val parser =
            SimpleDateFormat(context.getString(R.string.export_share_file_datetime), Locale.ENGLISH)
        val fileName = context.getString(
            if (isFiltered) R.string.export_filtered_file_name else R.string.export_all_file_name,
            parser.format(Date())
        )

        val directory = File(context.cacheDir, "exported_data")
        directory.mkdir()

        val file = File(directory, fileName)
        file.createNewFile()
        file.printWriter().use {
            it.write(csvContent)
        }

        return fileName

    }

    private fun shareExportedFile(context: Context, fileName: String) {

        val directory = File(context.cacheDir, "exported_data")
        val contentFile = File(directory, fileName)
        val fileProvider = FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.provider", contentFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/comma-separated-values"
            putExtra(
                Intent.EXTRA_SUBJECT, context.getString(
                    R.string.export_email_subject, context.getString(R.string.app_name)
                )
            )
            putExtra(
                Intent.EXTRA_TEXT, context.getString(
                    R.string.export_email_message, context.getString(R.string.app_name)
                )
            )
            putExtra(Intent.EXTRA_STREAM, fileProvider)
        }

        startForResult.launch(
            Intent.createChooser(
                intent, context.getString(R.string.export_share_title)
            )
        )

    }

}