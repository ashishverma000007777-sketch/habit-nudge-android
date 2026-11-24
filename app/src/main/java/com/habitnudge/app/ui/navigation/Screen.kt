package com.habitnudge.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Reminders : Screen("reminders")
    object Stats : Screen("stats")
    object Journal : Screen("journal")
    object Settings : Screen("settings")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }
}

enum class BottomNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME(Screen.Home.route, "Home", Icons.Default.Home),
    REMINDERS(Screen.Reminders.route, "Nudges", Icons.Default.Notifications),
    STATS(Screen.Stats.route, "Progress", Icons.Default.BarChart),
    JOURNAL(Screen.Journal.route, "Journal", Icons.Default.Book),
    SETTINGS(Screen.Settings.route, "Settings", Icons.Default.Settings)
}
