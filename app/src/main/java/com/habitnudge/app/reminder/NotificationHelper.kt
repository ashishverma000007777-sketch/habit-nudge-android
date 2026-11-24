package com.habitnudge.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        const val CHANNEL_LEGACY = "habit_nudge_reminders_channel"
        const val CHANNEL_V2_PREFIX = "habit_nudge_v2"
        const val GROUP_KEY_HABIT_REMINDERS = "com.habitnudge.app.HABIT_REMINDERS"
        const val GROUP_SUMMARY_NOTIFICATION_ID = 999900
        const val TEST_NOTIFICATION_ID = 999901

        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"

        const val ACTION_MARK_DONE = "com.habitnudge.app.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.habitnudge.app.ACTION_SNOOZE"

        const val BRAND_ORANGE = 0xFFFF8A3D.toInt()
        const val BRAND_WARM_AMBER = 0xFFFFC078.toInt()

        val VIBRATION_PATTERN = longArrayOf(0, 180, 100, 240)

        fun resolveChannelId(
            soundEnabled: Boolean,
            vibrationEnabled: Boolean,
            soundToneUri: String?
        ): String {
            val soundPart = when {
                !soundEnabled -> "silent"
                soundToneUri.isNullOrEmpty() -> "default"
                else -> "tone_${soundToneUri.hashCode().toUInt().toString(16)}"
            }
            val vibPart = if (vibrationEnabled) "vib" else "novib"
            return "${CHANNEL_V2_PREFIX}_${soundPart}_${vibPart}"
        }

        fun getNotificationIdForHabit(habitId: String): Int {
            return (habitId.hashCode() and 0x7FFFFFFF).coerceAtLeast(100)
        }

        fun isInQuietHours(
            now: LocalTime,
            startHour: Int,
            startMinute: Int,
            endHour: Int,
            endMinute: Int
        ): Boolean {
            val start = LocalTime.of(startHour, startMinute)
            val end = LocalTime.of(endHour, endMinute)
            return if (start == end) {
                false
            } else if (start.isBefore(end)) {
                (!now.isBefore(start)) && now.isBefore(end)
            } else {
                (!now.isBefore(start)) || now.isBefore(end)
            }
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Safe channel migration: Remove obsolete v1 uncustomized channel if present
            try {
                if (notificationManager.getNotificationChannel(CHANNEL_LEGACY) != null) {
                    notificationManager.deleteNotificationChannel(CHANNEL_LEGACY)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Create default high-importance channel with sound and vibration
            ensureChannelExists(
                channelId = resolveChannelId(soundEnabled = true, vibrationEnabled = true, soundToneUri = null),
                soundEnabled = true,
                vibrationEnabled = true,
                soundToneUri = null
            )

            // Create silent channel for quiet reminders
            ensureChannelExists(
                channelId = resolveChannelId(soundEnabled = false, vibrationEnabled = false, soundToneUri = null),
                soundEnabled = false,
                vibrationEnabled = false,
                soundToneUri = null
            )
        }
    }

    private fun ensureChannelExists(
        channelId: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        soundToneUri: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(channelId)
            if (existing != null) {
                // Channel already configured; do not overwrite user-controlled system channel preferences
                return
            }

            val soundUri = if (soundEnabled) {
                if (!soundToneUri.isNullOrEmpty()) {
                    Uri.parse(soundToneUri)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
            } else {
                null
            }

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val channelName = if (soundEnabled) "Habit Nudges" else "Habit Nudges (Silent)"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily habit reminders, motivational prompts and progress check-ins"
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) {
                    vibrationPattern = VIBRATION_PATTERN
                } else {
                    vibrationPattern = longArrayOf(0)
                }
                setSound(soundUri, if (soundEnabled) audioAttributes else null)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun showHabitNotification(
        habit: HabitEntity,
        reminderId: Long,
        customMessage: String? = null,
        isTest: Boolean = false
    ): Boolean {
        val settings = preferencesRepository.userSettingsFlow.first()

        if (!isTest) {
            if (!settings.notificationsEnabled) return false

            // Check Quiet Hours
            if (settings.quietHoursEnabled) {
                val inQuietHours = isInQuietHours(
                    now = LocalTime.now(),
                    startHour = settings.quietHoursStartHour,
                    startMinute = settings.quietHoursStartMinute,
                    endHour = settings.quietHoursEndHour,
                    endMinute = settings.quietHoursEndMinute
                )
                if (inQuietHours) {
                    return false
                }
            }
        }

        val channelId = resolveChannelId(
            soundEnabled = settings.reminderSoundEnabled,
            vibrationEnabled = settings.reminderVibrationEnabled,
            soundToneUri = settings.soundToneUri
        )

        ensureChannelExists(
            channelId = channelId,
            soundEnabled = settings.reminderSoundEnabled,
            vibrationEnabled = settings.reminderVibrationEnabled,
            soundToneUri = settings.soundToneUri
        )

        val notificationId = if (isTest) TEST_NOTIFICATION_ID else getNotificationIdForHabit(habit.id)
        val identity = HabitNotificationRegistry.getIdentity(habit.id, habit.title)
        val message = customMessage ?: identity.message

        // Content Intent: Opens habit detail screen in MainActivity
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

        // Action 1: Mark Done
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

        // Action 2: Snooze 10 Minutes
        val snooze10Intent = Intent(context, HabitNotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_HABIT_ID, habit.id)
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_SNOOZE_MINUTES, 10)
        }
        val snooze10PendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snooze10Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 3: Snooze 30 Minutes
        val snooze30Intent = Intent(context, HabitNotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_HABIT_ID, habit.id)
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_SNOOZE_MINUTES, 30)
        }
        val snooze30PendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 3,
            snooze30Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullTitle = "${identity.emoji} ${habit.title}"
        val largeIcon = NotificationIconGenerator.createHabitLargeIconSafe(
            context = context,
            emoji = identity.emoji,
            accentColorHex = identity.accentColorHex
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_nudge)
            .setColor(BRAND_ORANGE)
            .setContentTitle(fullTitle)
            .setContentText(message)
            .setSubText("Habit Nudge")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(fullTitle)
                    .setSummaryText("Habit Nudge • ${identity.category}")
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(contentPendingIntent)
            .setGroup(GROUP_KEY_HABIT_REMINDERS)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .addAction(0, "Mark Done", markDonePendingIntent)
            .addAction(0, "Snooze 10m", snooze10PendingIntent)
            .addAction(0, "Snooze 30m", snooze30PendingIntent)

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }

        // Silent mode only if both sound and vibration are turned off
        if (!settings.reminderSoundEnabled && !settings.reminderVibrationEnabled) {
            builder.setSilent(true)
        }

        notificationManager.notify(notificationId, builder.build())
        return true
    }

    suspend fun showTestNotification(habitId: String? = null): Boolean {
        val targetHabitId = habitId ?: "daily_hydration"
        val identity = HabitNotificationRegistry.getIdentity(targetHabitId)
        val dummyHabit = HabitEntity(
            id = identity.habitId,
            title = identity.title,
            tagline = "Test Notification Preview",
            description = "Test notification verifying sound engine and visual identity",
            durationDays = 21,
            tasksJson = "[]",
            category = identity.category,
            accentColorHex = identity.accentColorHex
        )
        return showHabitNotification(
            habit = dummyHabit,
            reminderId = -1L,
            customMessage = identity.message,
            isTest = true
        )
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isChannelBlocked(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE
        }
        return false
    }
}
