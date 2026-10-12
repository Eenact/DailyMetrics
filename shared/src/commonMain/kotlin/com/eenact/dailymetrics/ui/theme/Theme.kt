package com.eenact.dailymetrics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colors carried over from the original res/values/colors.xml.
val BrandPrimary = Color(0xFF4372C4)
val BrandPrimaryLight = Color(0xFF558DF0)
val BrandPrimaryDark = Color(0xFF1F3864)
val SecondaryText = Color(0xFF9E9E9E)

/** Preset legend colors offered by the color picker. */
val LegendColors: List<Int> = listOf(
    0xFF1F77B4, // blue
    0xFFE17F0E, // orange
    0xFF2CA02C, // green
    0xFFD62728, // red
    0xFF9467BD, // purple
    0xFF8C564B, // brown
    0xFFE377C2, // pink
    0xFF7F7F7F, // grey
    0xFFBCBD22, // light green
).map { it.toInt() }

/** Default legend color of a new task (legend_red). */
val DefaultLegendColor: Int = 0xFFD62728.toInt()

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimary,
    onPrimaryContainer = Color.White,
    secondaryContainer = BrandPrimaryLight,
    onSecondaryContainer = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = BrandPrimary,
    onPrimaryContainer = Color.White,
    secondaryContainer = BrandPrimary,
    onSecondaryContainer = Color.White,
)

@Composable
fun DailyMetricsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
