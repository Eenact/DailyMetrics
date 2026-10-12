package com.eenact.dailymetrics.ui.legal

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eenact.dailymetrics.di.LocalAppContainer
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.app_name
import com.eenact.dailymetrics.resources.back
import com.eenact.dailymetrics.resources.ic_arrow_back
import com.eenact.dailymetrics.resources.legal_app
import com.eenact.dailymetrics.resources.legal_libraries
import com.eenact.dailymetrics.resources.legal_title
import com.eenact.dailymetrics.ui.theme.DailyMetricsTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LegalScreen(onBack: () -> Unit) {
    val appInfo = LocalAppContainer.current.appInfo
    LegalContent(versionName = appInfo.versionName, buildNumber = appInfo.buildNumber, onBack = onBack)
}

@Composable
private fun LegalContent(versionName: String, buildNumber: String, onBack: () -> Unit) {
    val header = stringResource(Res.string.legal_app, stringResource(Res.string.app_name), versionName, buildNumber)
    val libraries = stringResource(Res.string.legal_libraries)
    val text = remember(header, libraries) {
        AnnotatedString.fromHtml("$header<br><br>$libraries<br><br>$APACHE_2_LICENSE_HTML")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.legal_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(painterResource(Res.drawable.ic_arrow_back), stringResource(Res.string.back)) }
                },
            )
        },
    ) { padding ->
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Preview
@Composable
private fun LegalScreenPreview() {
    DailyMetricsTheme {
        LegalContent(versionName = "2.0.0", buildNumber = "202610031", onBack = {})
    }
}
