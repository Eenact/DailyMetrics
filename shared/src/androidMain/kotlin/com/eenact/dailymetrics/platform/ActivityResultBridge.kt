package com.eenact.dailymetrics.platform

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.eenact.dailymetrics.domain.CsvFile
import kotlinx.coroutines.CompletableDeferred

/**
 * Turns the callback based Activity Result APIs into suspend functions.
 *
 * Must be created in [ComponentActivity.onCreate], before the activity is started.
 */
class ActivityResultBridge(private val activity: ComponentActivity) {

    private var pendingPermission: CompletableDeferred<Boolean>? = null
    private var pendingDocument: CompletableDeferred<Uri?>? = null

    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            pendingPermission?.complete(granted)
        }

    private val createDocumentLauncher =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument(CsvFile.MIME_TYPE)) { uri ->
            pendingDocument?.complete(uri)
        }

    suspend fun requestNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) return true
        val deferred = CompletableDeferred<Boolean>().also { pendingPermission = it }
        permissionLauncher.launch(permission)
        return deferred.await()
    }

    /** Opens the system "save as" picker. Returns null when the user cancels. */
    suspend fun createDocument(fileName: String): Uri? {
        val deferred = CompletableDeferred<Uri?>().also { pendingDocument = it }
        createDocumentLauncher.launch(fileName)
        return deferred.await()
    }
}
