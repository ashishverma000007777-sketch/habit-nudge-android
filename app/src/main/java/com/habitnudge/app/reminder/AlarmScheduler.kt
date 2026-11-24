package com.habitnudge.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.habitnudge.app.data.local.dao.HabitDao
import com.habitnudge.app.data.local.dao.ReminderDao
import com.habitnudge.app.data.local.entity.ReminderEntity
import com.habitnudge.app.reminder.receiver.HabitReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: ReminderDao,
    private val habitDao: HabitDao
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(reminder: ReminderEntity) {
        if (!reminder.isEnabled) {
            cancelReminder(reminder.id)
            return
        }

        val triggerMillis = if (reminder.snoozeUntilMillis != null && reminder.snoozeUntilMillis > System.currentTimeMillis()) {
            reminder.snoozeUntilMillis
        } else {
            calculateNextTriggerMillis(reminder.timeHour, reminder.timeMinute, reminder.repeatDaysCsv)
        }

        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminder.id)
            putExtra(NotificationHelper.EXTRA_HABIT_ID, reminder.habitId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun scheduleOneOffReminder(habitId: String, triggerMillis: Long) {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
            putExtra(NotificationHelper.EXTRA_HABIT_ID, habitId)
        }

        val requestCode = ((habitId.hashCode() and 0x7FFFFFFF) % 100000) + 800000
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(reminderId: Long) {
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    suspend fun rescheduleAllAlarms() {
        val enabledReminders = reminderDao.getEnabledRemindersList()

        for (reminder in enabledReminders) {
            val habit = habitDao.getHabitById(reminder.habitId)
            if (habit != null && habit.isActive && !habit.isPaused) {
                scheduleReminder(reminder)
            } else {
                cancelReminder(reminder.id)
            }
        }
    }

    private fun calculateNextTriggerMillis(hour: Int, minute: Int, repeatDaysCsv: String): Long {
        val now = LocalDateTime.now()
        val reminderTime = LocalTime.of(hour, minute)

        val repeatDays = repeatDaysCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()

        val validDays = if (repeatDays.isEmpty()) (1..7).toSet() else repeatDays

        for (i in 0..14) {
            val targetDate = now.toLocalDate().plusDays(i.toLong())
            val targetDayOfWeek = targetDate.dayOfWeek.value // 1 = Monday .. 7 = Sunday
            if (validDays.contains(targetDayOfWeek)) {
                val targetDateTime = LocalDateTime.of(targetDate, reminderTime)
                if (targetDateTime.isAfter(now)) {
                    return targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }
        }

        return now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
