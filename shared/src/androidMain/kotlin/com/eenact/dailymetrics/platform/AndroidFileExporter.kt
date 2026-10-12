package com.eenact.dailymetrics.platform

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.app_name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import java.io.File

/**
 * Shares through a FileProvider (authority "<applicationId>.provider", declared by the app module)
 * and saves through the Storage Access Framework.
 */
class AndroidFileExporter(
    private val activity: Activity,
    private val bridge: ActivityResultBridge,
) : FileExporter {

    override suspend fun share(fileName: String, mimeType: String, content: String) {
        val file = withContext(Dispatchers.IO) {
            File(activity.cacheDir, "exported_data").apply { mkdirs() }
                .resolve(fileName)
                .apply { writeText(content) }
        }
        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.provider", file)
        val appName = getString(Res.string.app_name)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, "#$appName: exported data")
            putExtra(Intent.EXTRA_TEXT, "My task records collected from #$appName.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(Intent.createChooser(intent, "Share file …"))
    }

    override suspend fun save(fileName: String, mimeType: String, content: String): Boolean {
        val uri = bridge.createDocument(fileName) ?: return false
        return withContext(Dispatchers.IO) {
            activity.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) } != null
        }
    }
}
