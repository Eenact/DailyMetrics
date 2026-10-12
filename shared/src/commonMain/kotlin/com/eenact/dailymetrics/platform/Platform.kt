package com.eenact.dailymetrics.platform

import androidx.compose.runtime.Composable

/** Whether the user's device prefers a 24 hour clock. */
@Composable
expect fun is24HourFormat(): Boolean
