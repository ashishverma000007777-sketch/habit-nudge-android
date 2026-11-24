package com.habitnudge.app.data.backup

import com.habitnudge.app.data.local.entity.HabitCheckInEntity
import com.habitnudge.app.data.local.entity.NightJournalEntity
import com.habitnudge.app.data.local.entity.ReminderEntity
import com.habitnudge.app.data.preferences.AppTheme
import com.habitnudge.app.data.preferences.UserSettings
import com.habitnudge.app.ui.components.formatRepeatSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassDesignAndBackupTest {

    @Test
    fun `backup serialization and parsing round-trips all data and settings`() {
        val original = BackupPayload(
            version = 1,
            exportedAtMillis = 1732470000000L,
            habitStates = listOf(
                HabitStateBackup("daily_hydration", isActive = true, startDate = 1732400000000L, isPaused = false),
                HabitStateBackup("meditation", isActive = false, startDate = null, isPaused = true)
            ),
            checkIns = listOf(
                HabitCheckInEntity(habitId = "daily_hydration", date = "2026-11-24", timestamp = 1732470000000L, completed = true)
            ),
            reminders = listOf(
                ReminderEntity(
                    habitId = "daily_hydration",
                    timeHour = 10,
                    timeMinute = 30,
                    isEnabled = true,
                    repeatDaysCsv = "1,2,3,4,5",
                    label = "Morning Hydration"
                )
            ),
            journals = listOf(
                NightJournalEntity(
                    date = "2026-11-24",
                    timestamp = 1732470000000L,
                    wentWell = "Drank water consistently",
                    wastedTimeOrEnergy = "Late afternoon scrolling",
                    improveTomorrow = "Start meditation at 9 AM"
                )
            ),
            settings = UserSettings(
                theme = AppTheme.LIGHT,
                notificationsEnabled = true,
                quietHoursEnabled = true,
                quietHoursStartHour = 22,
                quietHoursStartMinute = 30,
                quietHoursEndHour = 6,
                quietHoursEndMinute = 45,
                reminderSoundEnabled = false,
                reminderVibrationEnabled = true,
                soundToneUri = "content://tone/1",
                soundToneTitle = "Soft Bell",
                repeatingAlarmEnabled = true,
                alarmMaxDurationSeconds = 90,
                alarmVolume = 0.7f,
                alarmToneUri = "content://alarm/2",
                alarmToneTitle = "Dawn Chime",
                weekStartMonday = false,
                bedtimeHour = 22,
                bedtimeMinute = 45,
                isOnboardingCompleted = true
            )
        )

        val json = BackupRestoreManager.serializePayload(original)
        val restored = BackupRestoreManager.parsePayload(json)

        assertEquals(1, restored.version)
        assertEquals(2, restored.habitStates.size)
        assertTrue(restored.habitStates[0].isActive)
        assertNull(restored.habitStates[1].startDate)
        assertTrue(restored.habitStates[1].isPaused)

        assertEquals(1, restored.checkIns.size)
        assertEquals("2026-11-24", restored.checkIns[0].date)

        assertEquals(1, restored.reminders.size)
        assertEquals(10, restored.reminders[0].timeHour)
        assertEquals("1,2,3,4,5", restored.reminders[0].repeatDaysCsv)
        assertEquals("Morning Hydration", restored.reminders[0].label)

        assertEquals(1, restored.journals.size)
        assertEquals("Drank water consistently", restored.journals[0].wentWell)

        assertEquals(AppTheme.LIGHT, restored.settings.theme)
        assertEquals(90, restored.settings.alarmMaxDurationSeconds)
        assertEquals(0.7f, restored.settings.alarmVolume, 0.001f)
        assertEquals("Dawn Chime", restored.settings.alarmToneTitle)
        assertEquals(false, restored.settings.weekStartMonday)
        assertEquals(22, restored.settings.bedtimeHour)
        assertEquals(45, restored.settings.bedtimeMinute)
    }

    @Test
    fun `formatRepeatSchedule produces human-readable cadence labels`() {
        assertEquals("Daily", formatRepeatSchedule("1,2,3,4,5,6,7"))
        assertEquals("Daily", formatRepeatSchedule(""))
        assertEquals("Weekdays", formatRepeatSchedule("1,2,3,4,5"))
        assertEquals("Weekends", formatRepeatSchedule("6,7"))
        assertEquals("Mon, Wed, Fri", formatRepeatSchedule("5,1,3"))
        assertEquals("Sat", formatRepeatSchedule("6"))
    }
}
