package com.habitnudge.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habitnudge.app.data.backup.BackupRestoreManager
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.data.repository.JournalRepository
import com.habitnudge.app.data.repository.ReminderRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.reminder.NotificationHelper
import com.habitnudge.app.ui.screens.detail.HabitDetailScreen
import com.habitnudge.app.ui.screens.home.HomeScreen
import com.habitnudge.app.ui.screens.journal.NightJournalScreen
import com.habitnudge.app.ui.screens.onboarding.OnboardingScreen
import com.habitnudge.app.ui.screens.reminders.ReminderCenterScreen
import com.habitnudge.app.ui.screens.settings.SettingsScreen
import com.habitnudge.app.ui.screens.splash.SplashScreen
import com.habitnudge.app.ui.screens.stats.ProgressStatsScreen

@Composable
fun HabitNudgeNavHost(
    navController: NavHostController,
    habitRepository: HabitRepository,
    reminderRepository: ReminderRepository,
    journalRepository: JournalRepository,
    preferencesRepository: UserPreferencesRepository,
    alarmScheduler: AlarmScheduler,
    notificationHelper: NotificationHelper,
    backupRestoreManager: BackupRestoreManager,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                preferencesRepository = preferencesRepository,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                habitRepository = habitRepository,
                preferencesRepository = preferencesRepository,
                alarmScheduler = alarmScheduler,
                onFinishOnboarding = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                habitRepository = habitRepository,
                onNavigate = { route ->
                    if (route != Screen.Home.route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            HabitDetailScreen(
                habitId = habitId,
                habitRepository = habitRepository,
                reminderRepository = reminderRepository,
                preferencesRepository = preferencesRepository,
                alarmScheduler = alarmScheduler,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reminders.route) {
            ReminderCenterScreen(
                reminderRepository = reminderRepository,
                habitRepository = habitRepository,
                preferencesRepository = preferencesRepository,
                alarmScheduler = alarmScheduler,
                onNavigate = { route ->
                    if (route != Screen.Reminders.route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.Stats.route) {
            ProgressStatsScreen(
                habitRepository = habitRepository,
                onNavigate = { route ->
                    if (route != Screen.Stats.route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.Journal.route) {
            NightJournalScreen(
                journalRepository = journalRepository,
                onNavigate = { route ->
                    if (route != Screen.Journal.route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                preferencesRepository = preferencesRepository,
                habitRepository = habitRepository,
                alarmScheduler = alarmScheduler,
                notificationHelper = notificationHelper,
                backupRestoreManager = backupRestoreManager,
                onNavigate = { route ->
                    if (route != Screen.Settings.route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
