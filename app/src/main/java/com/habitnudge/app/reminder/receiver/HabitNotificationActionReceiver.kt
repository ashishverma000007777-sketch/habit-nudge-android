package com.habitnudge.app.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitnudge.app.data.local.dao.ReminderDao
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.reminder.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HabitNotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val habitId = intent.getStringExtra(NotificationHelper.EXTRA_HABIT_ID) ?: return
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (notificationId != -1) {
                    notificationHelper.cancelNotification(notificationId)
                }

                when (action) {
                    NotificationHelper.ACTION_MARK_DONE -> {
                        habitRepository.markHabitCompletedForToday(habitId)
                    }
                    NotificationHelper.ACTION_SNOOZE -> {
                        val snoozeMinutes = intent.getIntExtra(NotificationHelper.EXTRA_SNOOZE_MINUTES, 10)
                        val snoozeMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
                        if (reminderId != -1L) {
                            reminderDao.updateSnooze(reminderId, snoozeMillis)
                            val reminder = reminderDao.getReminderById(reminderId)
                            if (reminder != null) {
                                alarmScheduler.scheduleReminder(reminder)
                            }
                        } else {
                            // Support one-off snooze for notifications without stored reminder record (e.g. test)
                            alarmScheduler.scheduleOneOffReminder(habitId, snoozeMillis)
                        }
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
