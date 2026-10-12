package com.eenact.dailymetrics.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume

/** Share sheet for "Share file" and the Files export picker for "Save file". */
class IosFileExporter : FileExporter {

    // UIDocumentPickerViewController holds its delegate weakly.
    private var pickerDelegate: NSObject? = null

    override suspend fun share(fileName: String, mimeType: String, content: String) {
        val url = writeTemporaryFile(fileName, content) ?: return
        val presenter = topViewController() ?: return
        val controller = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
        // Required on iPad, where the share sheet is a popover.
        controller.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(controller, animated = true, completion = null)
    }

    override suspend fun save(fileName: String, mimeType: String, content: String): Boolean {
        val url = writeTemporaryFile(fileName, content) ?: return false
        val presenter = topViewController() ?: return false
        return suspendCancellableCoroutine { continuation ->
            val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                    pickerDelegate = null
                    if (continuation.isActive) continuation.resume(true)
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    pickerDelegate = null
                    if (continuation.isActive) continuation.resume(false)
                }
            }
            pickerDelegate = delegate
            val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url), asCopy = true)
            picker.delegate = delegate
            presenter.presentViewController(picker, animated = true, completion = null)
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun writeTemporaryFile(fileName: String, content: String): NSURL? {
        val url = NSURL.fileURLWithPath(NSTemporaryDirectory() + fileName)
        val written = NSString.create(string = content).writeToURL(url, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        return if (written) url else null
    }

    private fun topViewController(): UIViewController? {
        @Suppress("DEPRECATION")
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (controller?.presentedViewController != null) controller = controller.presentedViewController
        return controller
    }
}
