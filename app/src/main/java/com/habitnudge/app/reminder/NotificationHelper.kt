package com.habitnudge.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.habitnudge.app.MainActivity
import com.habitnudge.app.R
import com.habitnudge.app.data.local.entity.HabitEntity
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.reminder.receiver.HabitNotificationActionReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    companion object {
        const val CHANNEL_REMINDERS = "habit_nudge_reminders_channel"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        const val ACTION_MARK_DONE = "com.habitnudge.app.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.habitnudge.app.ACTION_SNOOZE"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Habit Nudges",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily habit reminders and nudges"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun showHabitNotification(
        habit: HabitEntity,
        reminderId: Long,
        customMessage: String? = null
    ) {
        val settings = preferencesRepository.userSettingsFlow.first()
        if (!settings.notificationsEnabled) return

        // Check Quiet Hours
        if (settings.quietHoursEnabled) {
            val now = LocalTime.now()
            val start = LocalTime.of(settings.quietHoursStartHour, settings.quietHoursStartMinute)
            val end = LocalTime.of(settings.quietHoursEndHour, settings.quietHoursEndMinute)
            val inQuietHours = if (start.isBefore(end)) {
                (!now.isBefore(start)) && now.isBefore(end)
            } else {
                // e.g. 22:00 to 07:00 (spans midnight)
                (!now.isBefore(start)) || now.isBefore(end)
            }
            if (inQuietHours) {
                // Suppressed during quiet hours
                return
            }
        }

        val notificationId = (habit.id.hashCode() + reminderId.toInt()).coerceAtLeast(1)

        // Intent to open MainActivity and navigate to habit detail
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_HABIT_ID, habit.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Done Action
        val markDoneIntent = Intent(context, HabitNotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_HABIT_ID, habit.id)
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 10m Action
        val snoozeIntent = Intent(context, HabitNotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_HABIT_ID, habit.id)
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = customMessage ?: getNudgeMessageForHabit(habit.id)

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_nudge)
            .setContentTitle(habit.title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Mark Done", markDonePendingIntent)
            .addAction(0, "Snooze 10m", snoozePendingIntent)

        if (!settings.reminderSoundEnabled) {
            builder.setSilent(true)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun getNudgeMessageForHabit(habitId: String): String {
        return when (habitId) {
            "reduce_added_sugar" -> "Keep your momentum! Choose water or whole fruit today."
            "daily_hydration" -> "Time for a mindful sip. Keep your water nearby."
            "no_phone_before_bed" -> "Wind down: Put your phone away and prepare for deep rest."
            "meditation" -> "Take a 10-minute pause to reset your breath and calm your thoughts."
            "cool_water_shower" -> "Invigorate your nervous system with a refreshing cool rinse."
            "three_hours_alone" -> "Your solo recharge window is here. Disconnect from screens."
            "night_journaling" -> "Three quick questions to capture today's wins and set tomorrow's focus."
            else -> "It's time for your daily habit nudge. Small steps lead to great habits."
        }
    }
}
