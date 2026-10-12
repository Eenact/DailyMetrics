package com.eenact.dailymetrics.platform

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.room3.Room
import com.eenact.dailymetrics.data.db.AppDatabase
import com.eenact.dailymetrics.data.db.DATABASE_NAME
import com.eenact.dailymetrics.data.db.buildAppDatabase

@Composable
actual fun is24HourFormat(): Boolean = DateFormat.is24HourFormat(LocalContext.current)

/** Process-wide database instance, shared by the UI and the broadcast receivers. */
object AndroidDatabase {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
        instance ?: run {
            val appContext = context.applicationContext
            buildAppDatabase(
                Room.databaseBuilder<AppDatabase>(
                    context = appContext,
                    name = appContext.getDatabasePath(DATABASE_NAME).absolutePath,
                ),
            ).also { instance = it }
        }
    }
}

/** Reuses the preference file and key of the original app so existing users skip onboarding. */
class AndroidAppSettings(context: Context) : AppSettings {
    private val preferences = context.applicationContext.getSharedPreferences("stspref", Context.MODE_PRIVATE)

    override var isAppOnboarded: Boolean
        get() = preferences.getBoolean("STS_ID_APP_ONBOARDED", false)
        set(value) = preferences.edit { putBoolean("STS_ID_APP_ONBOARDED", value) }
}

fun androidAppInfo(context: Context): AppInfo {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    @Suppress("DEPRECATION")
    val build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else info.versionCode.toLong()
    return AppInfo(versionName = info.versionName.orEmpty(), buildNumber = build.toString())
}
