package com.habitnudge.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.habitnudge.app.data.preferences.AppTheme
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.data.repository.JournalRepository
import com.habitnudge.app.data.repository.ReminderRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.reminder.NotificationHelper
import com.habitnudge.app.ui.navigation.HabitNudgeNavHost
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.HabitNudgeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var journalRepository: JournalRepository

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Permission result handled gracefully
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val habitIdExtra = intent?.getStringExtra(NotificationHelper.EXTRA_HABIT_ID)

        setContent {
            val userSettings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
            val currentTheme = userSettings?.theme ?: AppTheme.DARK

            HabitNudgeTheme(appTheme = currentTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    HabitNudgeNavHost(
                        navController = navController,
                        habitRepository = habitRepository,
                        reminderRepository = reminderRepository,
                        journalRepository = journalRepository,
                        preferencesRepository = preferencesRepository,
                        alarmScheduler = alarmScheduler,
                        startDestination = if (habitIdExtra != null) Screen.HabitDetail.createRoute(habitIdExtra) else Screen.Splash.route
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
