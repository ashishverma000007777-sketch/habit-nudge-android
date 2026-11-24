package com.habitnudge.app.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitnudge.app.data.local.dao.CheckInDao
import com.habitnudge.app.data.local.dao.HabitDao
import com.habitnudge.app.data.local.dao.ReminderDao
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.reminder.NotificationHelper
import com.habitnudge.app.reminder.alarm.HabitAlarmSoundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class HabitReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitDao: HabitDao

    @Inject
    lateinit var checkInDao: CheckInDao

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(NotificationHelper.EXTRA_HABIT_ID) ?: return
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habit = habitDao.getHabitById(habitId)
                if (habit == null || !habit.isActive || habit.isPaused) {
                    return@launch
                }

                // Check if habit is already completed today
                val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val checkIn = checkInDao.getCheckIn(habitId, todayStr)
                if (checkIn != null && checkIn.completed) {
                    // Habit already completed today; skip notification
                    return@launch
                }

                val settings = preferencesRepository.userSettingsFlow.first()
                if (!settings.notificationsEnabled) {
                    return@launch
                }

                // Check Quiet Hours
                if (settings.quietHoursEnabled) {
                    val inQuietHours = NotificationHelper.isInQuietHours(
                        now = LocalTime.now(),
                        startHour = settings.quietHoursStartHour,
                        startMinute = settings.quietHoursStartMinute,
                        endHour = settings.quietHoursEndHour,
                        endMinute = settings.quietHoursEndMinute
                    )
                    if (inQuietHours) {
                        return@launch
                    }
                }

                if (settings.repeatingAlarmEnabled) {
                    // Trigger repeating alarm foreground service
                    HabitAlarmSoundService.startAlarm(
                        context = context,
                        habitId = habitId,
                        reminderId = reminderId,
                        maxDurationSeconds = settings.alarmMaxDurationSeconds,
                        volume = settings.alarmVolume,
                        toneUri = settings.alarmToneUri,
                        vibrationEnabled = settings.reminderVibrationEnabled,
                        isTest = false
                    )
                } else {
                    // Standard notification with one-shot sound
                    notificationHelper.showHabitNotification(habit, reminderId)
                }

                // Clear snooze and schedule next recurrence
                if (reminderId != -1L) {
                    reminderDao.updateSnooze(reminderId, null)
                    val reminder = reminderDao.getReminderById(reminderId)
                    if (reminder != null && reminder.isEnabled) {
                        alarmScheduler.scheduleReminder(reminder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
