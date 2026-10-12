package com.eenact.dailymetrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.eenact.dailymetrics.di.AppContainer
import com.eenact.dailymetrics.di.LocalAppContainer
import com.eenact.dailymetrics.ui.dashboard.DashboardScreen
import com.eenact.dailymetrics.ui.entry.TaskEntryScreen
import com.eenact.dailymetrics.ui.legal.LegalScreen
import com.eenact.dailymetrics.ui.onboarding.OnboardingScreen
import com.eenact.dailymetrics.ui.tasks.TaskEditScreen
import com.eenact.dailymetrics.ui.tasks.TaskManagerScreen
import com.eenact.dailymetrics.ui.theme.DailyMetricsTheme

/** Screens of the app. The back stack is a plain list of these. */
sealed interface Route {
    data object Onboarding : Route
    data object Dashboard : Route
    data object TaskEntry : Route
    data object TaskManager : Route
    /** Create a task when [taskId] is null, otherwise edit it. */
    data class TaskEdit(val taskId: Long?) : Route
    data object Legal : Route
}

/** Root composable shared by Android, iOS and (later) web. */
@Composable
fun DailyMetricsApp(container: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides container) {
        DailyMetricsTheme {
            val backStack = remember {
                mutableStateListOf<Route>(if (container.settings.isAppOnboarded) Route.Dashboard else Route.Onboarding)
            }
            val back: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

            NavDisplay(
                backStack = backStack,
                onBack = back,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<Route.Onboarding> {
                        OnboardingScreen(
                            onFinished = {
                                backStack.clear()
                                backStack.add(Route.Dashboard)
                            },
                        )
                    }
                    entry<Route.Dashboard> {
                        DashboardScreen(
                            onAddValue = { backStack.add(Route.TaskEntry) },
                            onManageTasks = { backStack.add(Route.TaskManager) },
                            onLegalTerms = { backStack.add(Route.Legal) },
                        )
                    }
                    entry<Route.TaskEntry> {
                        TaskEntryScreen(onClose = back)
                    }
                    entry<Route.TaskManager> {
                        TaskManagerScreen(
                            onBack = back,
                            onAddTask = { backStack.add(Route.TaskEdit(null)) },
                            onEditTask = { backStack.add(Route.TaskEdit(it)) },
                        )
                    }
                    entry<Route.TaskEdit> { route ->
                        TaskEditScreen(taskId = route.taskId, onBack = back, onSaved = back)
                    }
                    entry<Route.Legal> {
                        LegalScreen(onBack = back)
                    }
                },
            )
        }
    }
}
