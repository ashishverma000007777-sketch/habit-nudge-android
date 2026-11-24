package com.habitnudge.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_nudge_settings")

enum class AppTheme {
    SYSTEM,
    DARK,
    LIGHT
}

data class UserSettings(
    val theme: AppTheme = AppTheme.DARK,
    val notificationsEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStartHour: Int = 22,
    val quietHoursStartMinute: Int = 0,
    val quietHoursEndHour: Int = 7,
    val quietHoursEndMinute: Int = 0,
    val reminderSoundEnabled: Boolean = true,
    val reminderVibrationEnabled: Boolean = true,
    val soundToneUri: String = "",
    val soundToneTitle: String = "Default notification tone",
    val repeatingAlarmEnabled: Boolean = true,
    val alarmMaxDurationSeconds: Int = 60,
    val alarmVolume: Float = 0.85f,
    val alarmToneUri: String = "",
    val alarmToneTitle: String = "Default alarm sound",
    val weekStartMonday: Boolean = true,
    val bedtimeHour: Int = 23,
    val bedtimeMinute: Int = 0,
    val isOnboardingCompleted: Boolean = false
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_HOURS_START_HOUR = intPreferencesKey("quiet_hours_start_hour")
        val QUIET_HOURS_START_MINUTE = intPreferencesKey("quiet_hours_start_minute")
        val QUIET_HOURS_END_HOUR = intPreferencesKey("quiet_hours_end_hour")
        val QUIET_HOURS_END_MINUTE = intPreferencesKey("quiet_hours_end_minute")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_TONE_URI = stringPreferencesKey("sound_tone_uri")
        val SOUND_TONE_TITLE = stringPreferencesKey("sound_tone_title")
        val REPEATING_ALARM_ENABLED = booleanPreferencesKey("repeating_alarm_enabled")
        val ALARM_MAX_DURATION_SECONDS = intPreferencesKey("alarm_max_duration_seconds")
        val ALARM_VOLUME = floatPreferencesKey("alarm_volume")
        val ALARM_TONE_URI = stringPreferencesKey("alarm_tone_uri")
        val ALARM_TONE_TITLE = stringPreferencesKey("alarm_tone_title")
        val WEEK_START_MONDAY = booleanPreferencesKey("week_start_monday")
        val BEDTIME_HOUR = intPreferencesKey("bedtime_hour")
        val BEDTIME_MINUTE = intPreferencesKey("bedtime_minute")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userSettingsFlow: Flow<UserSettings> = dataStore.data.map { prefs ->
        val themeStr = prefs[PreferencesKeys.THEME] ?: AppTheme.DARK.name
        val theme = try { AppTheme.valueOf(themeStr) } catch (e: Exception) { AppTheme.DARK }
        UserSettings(
            theme = theme,
            notificationsEnabled = prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            quietHoursEnabled = prefs[PreferencesKeys.QUIET_HOURS_ENABLED] ?: true,
            quietHoursStartHour = prefs[PreferencesKeys.QUIET_HOURS_START_HOUR] ?: 22,
            quietHoursStartMinute = prefs[PreferencesKeys.QUIET_HOURS_START_MINUTE] ?: 0,
            quietHoursEndHour = prefs[PreferencesKeys.QUIET_HOURS_END_HOUR] ?: 7,
            quietHoursEndMinute = prefs[PreferencesKeys.QUIET_HOURS_END_MINUTE] ?: 0,
            reminderSoundEnabled = prefs[PreferencesKeys.SOUND_ENABLED] ?: true,
            reminderVibrationEnabled = prefs[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            soundToneUri = prefs[PreferencesKeys.SOUND_TONE_URI] ?: "",
            soundToneTitle = prefs[PreferencesKeys.SOUND_TONE_TITLE] ?: "Default notification tone",
            repeatingAlarmEnabled = prefs[PreferencesKeys.REPEATING_ALARM_ENABLED] ?: true,
            alarmMaxDurationSeconds = prefs[PreferencesKeys.ALARM_MAX_DURATION_SECONDS] ?: 60,
            alarmVolume = prefs[PreferencesKeys.ALARM_VOLUME] ?: 0.85f,
            alarmToneUri = prefs[PreferencesKeys.ALARM_TONE_URI] ?: "",
            alarmToneTitle = prefs[PreferencesKeys.ALARM_TONE_TITLE] ?: "Default alarm sound",
            weekStartMonday = prefs[PreferencesKeys.WEEK_START_MONDAY] ?: true,
            bedtimeHour = prefs[PreferencesKeys.BEDTIME_HOUR] ?: 23,
            bedtimeMinute = prefs[PreferencesKeys.BEDTIME_MINUTE] ?: 0,
            isOnboardingCompleted = prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[PreferencesKeys.THEME] = theme.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setQuietHours(enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        dataStore.edit {
            it[PreferencesKeys.QUIET_HOURS_ENABLED] = enabled
            it[PreferencesKeys.QUIET_HOURS_START_HOUR] = startHour
            it[PreferencesKeys.QUIET_HOURS_START_MINUTE] = startMinute
            it[PreferencesKeys.QUIET_HOURS_END_HOUR] = endHour
            it[PreferencesKeys.QUIET_HOURS_END_MINUTE] = endMinute
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setSoundTone(uri: String, title: String) {
        dataStore.edit {
            it[PreferencesKeys.SOUND_TONE_URI] = uri
            it[PreferencesKeys.SOUND_TONE_TITLE] = title
        }
    }

    suspend fun setRepeatingAlarmEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.REPEATING_ALARM_ENABLED] = enabled }
    }

    suspend fun setAlarmMaxDurationSeconds(seconds: Int) {
        dataStore.edit { it[PreferencesKeys.ALARM_MAX_DURATION_SECONDS] = seconds }
    }

    suspend fun setAlarmVolume(volume: Float) {
        dataStore.edit { it[PreferencesKeys.ALARM_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setAlarmTone(uri: String, title: String) {
        dataStore.edit {
            it[PreferencesKeys.ALARM_TONE_URI] = uri
            it[PreferencesKeys.ALARM_TONE_TITLE] = title
        }
    }

    suspend fun setWeekStartMonday(monday: Boolean) {
        dataStore.edit { it[PreferencesKeys.WEEK_START_MONDAY] = monday }
    }

    suspend fun setBedtime(hour: Int, minute: Int) {
        dataStore.edit {
            it[PreferencesKeys.BEDTIME_HOUR] = hour
            it[PreferencesKeys.BEDTIME_MINUTE] = minute
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }
}
