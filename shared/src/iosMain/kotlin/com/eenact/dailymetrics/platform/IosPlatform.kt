package com.eenact.dailymetrics.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.room3.Room
import com.eenact.dailymetrics.data.db.AppDatabase
import com.eenact.dailymetrics.data.db.DATABASE_NAME
import com.eenact.dailymetrics.data.db.buildAppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.currentLocale

@Composable
actual fun is24HourFormat(): Boolean = remember {
    // "j" expands to the locale's preferred hour pattern; it contains "a" for 12 hour clocks.
    NSDateFormatter.dateFormatFromTemplate("j", 0u, NSLocale.currentLocale)?.contains('a') != true
}

class IosAppSettings : AppSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    override var isAppOnboarded: Boolean
        get() = defaults.boolForKey(KEY_ONBOARDED)
        set(value) = defaults.setBool(value, forKey = KEY_ONBOARDED)

    private companion object {
        const val KEY_ONBOARDED = "STS_ID_APP_ONBOARDED"
    }
}

fun iosAppInfo(): AppInfo {
    val bundle = NSBundle.mainBundle
    return AppInfo(
        versionName = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "",
        buildNumber = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "",
    )
}

@OptIn(ExperimentalForeignApi::class)
fun createIosDatabase(): AppDatabase {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = requireNotNull(documents?.path) { "Documents directory unavailable" } + "/" + DATABASE_NAME
    return buildAppDatabase(Room.databaseBuilder<AppDatabase>(name = path))
}
