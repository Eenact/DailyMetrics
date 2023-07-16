package com.eenact.dailymetrics.managers

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.eenact.dailymetrics.BuildConfig
import com.eenact.dailymetrics.R
import com.eenact.dailymetrics.db.TaskEntity
import com.eenact.dailymetrics.db.TaskValue
import com.eenact.dailymetrics.enums.EntityType
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class DataExportManager(private val activity: FragmentActivity) : DefaultLifecycleObserver {

    companion object {
        private const val LINE_BREAK: String = "\n"
        private const val SAVE_KEY: String = "CONTENT_SAVE"
    }

    private var mFileName: String? = null
    private lateinit var mStartForResult: ActivityResultLauncher<Intent>

    // UI views
    private var mProgressView: View? = null
    private var mProgressDialog: AlertDialog? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        mStartForResult = activity.activityResultRegistry.register(
            SAVE_KEY, ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->

            activityResult.data?.data?.let { uri ->

                showProgressDialog()

                val inputAsString =
                    FileInputStream(mFileName).bufferedReader().use { it.readText() }

                activity.applicationContext.contentResolver.openOutputStream(uri).use { out ->
                    out?.write(inputAsString.toByteArray(), 0, inputAsString.length)
                    out?.flush()
                    out?.close()
                }
                fileSaveCompleted()

            }
        }
    }

    fun createCsvFile(
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
                        if (it.key.type == EntityType.YesNo) EntityType.YesNo.toString() else it.key.unit.toString(),
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

        mFileName = file.absolutePath

        return fileName

    }

    fun shareExportedFile(context: Context, fileName: String) {

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

        mStartForResult.launch(
            Intent.createChooser(
                intent, context.getString(R.string.export_share_title)
            )
        )

    }

    fun saveFile(fileName: String) {

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"

            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        mStartForResult.launch(intent)
    }

    private fun showProgressDialog() {

        mProgressView = LayoutInflater.from(activity).inflate(R.layout.dialog_progress, null)
        mProgressDialog =
            MaterialAlertDialogBuilder(activity).setTitle(R.string.export_progress_dialog_title)
                .setCancelable(true).setView(mProgressView).show()
    }

    private fun fileSaveCompleted() {

        mProgressView?.findViewById<CircularProgressIndicator>(R.id.dialog_progress_indicator)?.visibility =
            View.GONE
        mProgressView?.findViewById<TextView>(R.id.dialog_progress_text)?.visibility = View.VISIBLE

        Timer().schedule(2000) {
            mProgressDialog?.dismiss()
        }
    }
}