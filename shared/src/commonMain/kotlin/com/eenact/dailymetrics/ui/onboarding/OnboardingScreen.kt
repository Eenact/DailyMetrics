package com.eenact.dailymetrics.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eenact.dailymetrics.di.LocalAppContainer
import com.eenact.dailymetrics.platform.AppSettings
import com.eenact.dailymetrics.platform.is24HourFormat
import com.eenact.dailymetrics.resources.Res
import com.eenact.dailymetrics.resources.content_desc_next
import com.eenact.dailymetrics.resources.error_already_exists
import com.eenact.dailymetrics.resources.error_field_required
import com.eenact.dailymetrics.resources.ic_right_arrow
import com.eenact.dailymetrics.resources.onboarding_get_start
import com.eenact.dailymetrics.resources.onboarding_screen1_msg
import com.eenact.dailymetrics.resources.onboarding_screen1_title
import com.eenact.dailymetrics.resources.onboarding_screen2_msg
import com.eenact.dailymetrics.resources.onboarding_screen2_title
import com.eenact.dailymetrics.resources.onboarding_screen3_msg
import com.eenact.dailymetrics.resources.onboarding_screen3_title
import com.eenact.dailymetrics.resources.task_notification_permission_denied
import com.eenact.dailymetrics.ui.components.LottieAnimation
import com.eenact.dailymetrics.ui.components.TaskForm
import com.eenact.dailymetrics.ui.components.TaskFormState
import com.eenact.dailymetrics.ui.tasks.TaskSaveResult
import com.eenact.dailymetrics.ui.tasks.TaskSaver
import com.eenact.dailymetrics.ui.theme.DailyMetricsTheme
import com.eenact.dailymetrics.ui.theme.SecondaryText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val PAGE_COUNT = 3

class OnboardingViewModel(val saver: TaskSaver, private val settings: AppSettings) : ViewModel() {
    val form = TaskFormState()
    var isSaving by mutableStateOf(false)
        private set

    suspend fun finish(): TaskSaveResult {
        isSaving = true
        val result = saver.save(form)
        if (result == TaskSaveResult.Saved) settings.isAppOnboarded = true else isSaving = false
        return result
    }
}

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = viewModel {
        OnboardingViewModel(TaskSaver(container.repository, container.notificationScheduler), container.settings)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    OnboardingContent(
        form = viewModel.form,
        isSaving = viewModel.isSaving,
        snackbarHostState = snackbarHostState,
        onReminderToggled = { enabled ->
            scope.launch {
                if (!viewModel.saver.toggleReminder(viewModel.form, enabled)) {
                    snackbarHostState.showSnackbar(getString(Res.string.task_notification_permission_denied))
                }
            }
        },
        onFinish = {
            scope.launch {
                when (viewModel.finish()) {
                    TaskSaveResult.Saved -> onFinished()
                    TaskSaveResult.NameRequired -> viewModel.form.nameError = getString(Res.string.error_field_required)
                    TaskSaveResult.NameExists -> viewModel.form.nameError = getString(Res.string.error_already_exists)
                }
            }
        },
    )
}

/** Stateless onboarding UI, separate from the view model so it can be previewed. */
@Composable
private fun OnboardingContent(
    form: TaskFormState,
    isSaving: Boolean,
    onReminderToggled: (Boolean) -> Unit,
    onFinish: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    initialPage: Int = 0,
) {
    val pagerState = rememberPagerState(initialPage) { PAGE_COUNT }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { _ ->
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            HorizontalPager(state = pagerState, userScrollEnabled = false, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> IntroPage("files/lottie_onboarding_first.json", Res.string.onboarding_screen1_title, Res.string.onboarding_screen1_msg)
                    1 -> IntroPage("files/lottie_onboarding_second.json", Res.string.onboarding_screen2_title, Res.string.onboarding_screen2_msg)
                    else -> FirstTaskPage(form = form, onReminderToggled = onReminderToggled)
                }
            }

            Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                when {
                    isSaving -> CircularProgressIndicator()
                    pagerState.currentPage < PAGE_COUNT - 1 -> FloatingActionButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    ) {
                        Icon(painterResource(Res.drawable.ic_right_arrow), stringResource(Res.string.content_desc_next), Modifier.size(28.dp))
                    }
                    else -> Button(onClick = onFinish) {
                        Text(stringResource(Res.string.onboarding_get_start), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                    }
                }
            }

            PageIndicator(count = PAGE_COUNT, current = pagerState.currentPage, modifier = Modifier.padding(bottom = 24.dp))
        }
    }
}

@Composable
private fun IntroPage(animation: String, title: StringResource, message: StringResource) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LottieAnimation(animation, Modifier.fillMaxWidth().height(320.dp))
        Spacer(Modifier.height(32.dp))
        Text(
            stringResource(title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, color = SecondaryText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FirstTaskPage(form: TaskFormState, onReminderToggled: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Text(
            stringResource(Res.string.onboarding_screen3_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(Res.string.onboarding_screen3_msg),
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
        )
        TaskForm(
            state = form,
            onReminderToggled = onReminderToggled,
            is24Hour = is24HourFormat(),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        repeat(count) { index ->
            val color = MaterialTheme.colorScheme.primary
            Box(
                Modifier
                    .padding(horizontal = 8.dp)
                    .size(12.dp)
                    .then(if (index == current) Modifier.background(color, CircleShape) else Modifier.border(1.5.dp, color, CircleShape)),
            )
        }
    }
}
@Preview
@Composable
private fun OnboardingScreenPreview() {
    DailyMetricsTheme {
        OnboardingContent(
            form = remember { TaskFormState() },
            isSaving = false,
            onReminderToggled = {},
            onFinish = {},
            initialPage = PAGE_COUNT - 1,
        )
    }
}
