package com.habitnudge.app

import android.app.Application
import android.os.Build
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.reminder.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HabitNudgeApp : Application() {

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        notificationHelper.createNotificationChannels()

        // Ensure database seed data exists and schedule active alarms
        applicationScope.launch {
            habitRepository.ensureInitialized()
            alarmScheduler.rescheduleAllAlarms()
        }
    }
}
