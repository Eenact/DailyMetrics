package com.eenact.dailymetrics.managers

import android.content.Context
import android.content.SharedPreferences

object AppSettingsManager {

    private const val PREFERENCE_NAME = "stspref"
    private const val STS_ID_APP_ONBOARDED = "STS_ID_APP_ONBOARDED"

    fun instance(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    inline fun SharedPreferences.editStsId(operation: (SharedPreferences.Editor) -> Unit) {
        val editStsId = edit()
        operation(editStsId)
        editStsId.apply()
    }

    var SharedPreferences.isAppOnboarded
        get() = getBoolean(STS_ID_APP_ONBOARDED, false)
        set(value) {
            editStsId {
                it.putBoolean(STS_ID_APP_ONBOARDED, value)
            }
        }

}