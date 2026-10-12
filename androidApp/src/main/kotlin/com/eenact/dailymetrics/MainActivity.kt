package com.eenact.dailymetrics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eenact.dailymetrics.di.createAndroidAppContainer

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = createAndroidAppContainer(this)
        setContent { DailyMetricsApp(container) }
    }
}
