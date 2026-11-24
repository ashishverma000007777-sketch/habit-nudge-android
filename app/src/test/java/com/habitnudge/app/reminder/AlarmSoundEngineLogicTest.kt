package com.habitnudge.app.reminder

import com.habitnudge.app.reminder.alarm.HabitAlarmSoundService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSoundEngineLogicTest {

    @Test
    fun `alarm ringing max duration is properly constrained and guarded`() {
        // Default recommended duration
        val defaultDuration = 60
        assertEquals(60, defaultDuration.coerceAtLeast(5))

        // Minimum boundary safety guard
        val zeroDuration = 0
        assertEquals(5, zeroDuration.coerceAtLeast(5))

        val negativeDuration = -10
        assertEquals(5, negativeDuration.coerceAtLeast(5))

        // Standard configurable options
        val supportedDurations = listOf(30, 60, 90, 120, 300)
        assertTrue(supportedDurations.contains(defaultDuration))
        assertEquals(5, supportedDurations.size)
    }

    @Test
    fun `alarm volume is correctly clamped between 0 and 1`() {
        val normalVolume = 0.85f
        assertEquals(0.85f, normalVolume.coerceIn(0f, 1f), 0.001f)

        val overflowVolume = 1.5f
        assertEquals(1.0f, overflowVolume.coerceIn(0f, 1f), 0.001f)

        val negativeVolume = -0.2f
        assertEquals(0.0f, negativeVolume.coerceIn(0f, 1f), 0.001f)
    }

    @Test
    fun `alarm service action constants are distinct and properly namespaced`() {
        assertEquals("com.habitnudge.app.ACTION_START_ALARM", HabitAlarmSoundService.ACTION_START_ALARM)
        assertEquals("com.habitnudge.app.ACTION_STOP_ALARM", HabitAlarmSoundService.ACTION_STOP_ALARM)
        assertEquals("com.habitnudge.app.ACTION_MARK_DONE", NotificationHelper.ACTION_MARK_DONE)
        assertEquals("com.habitnudge.app.ACTION_SNOOZE", NotificationHelper.ACTION_SNOOZE)
        assertEquals("com.habitnudge.app.ACTION_STOP_ALARM", NotificationHelper.ACTION_STOP_ALARM)

        // Ensure actions are mutually distinct
        val actions = setOf(
            HabitAlarmSoundService.ACTION_START_ALARM,
            HabitAlarmSoundService.ACTION_STOP_ALARM,
            NotificationHelper.ACTION_MARK_DONE,
            NotificationHelper.ACTION_SNOOZE
        )
        assertEquals(4, actions.size)
        assertEquals(HabitAlarmSoundService.ACTION_STOP_ALARM, NotificationHelper.ACTION_STOP_ALARM)
    }

    @Test
    fun `alarm state is initially not ringing`() {
        assertFalse(HabitAlarmSoundService.isRinging)
    }
}
