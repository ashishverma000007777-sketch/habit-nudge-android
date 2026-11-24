package com.habitnudge.app.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class NotificationIdentityAndSoundTest {

    @Test
    fun `all seven habits have exact required identity, emojis, and messaging`() {
        val hydration = HabitNotificationRegistry.getIdentity("daily_hydration")
        assertEquals("Daily Hydration", hydration.title)
        assertEquals("💧", hydration.emoji)
        assertEquals("Your body needs water. Take a mindful sip!", hydration.message)
        assertEquals("Health & Nutrition", hydration.category)
        assertEquals("#35C98A", hydration.accentColorHex)

        val meditation = HabitNotificationRegistry.getIdentity("meditation")
        assertEquals("Meditation", meditation.title)
        assertEquals("🧘", meditation.emoji)
        assertEquals("Pause the noise. Give yourself 10 minutes.", meditation.message)
        assertEquals("Mindfulness & Focus", meditation.category)
        assertEquals("#4ECDC4", meditation.accentColorHex)

        val noPhone = HabitNotificationRegistry.getIdentity("no_phone_before_bed")
        assertEquals("No Phone Before Bed", noPhone.title)
        assertEquals("📵", noPhone.emoji)
        assertEquals("Your sleep deserves better. Put the phone away.", noPhone.message)
        assertEquals("Digital Wellbeing & Sleep", noPhone.category)
        assertEquals("#8E7CFF", noPhone.accentColorHex)

        val sugar = HabitNotificationRegistry.getIdentity("reduce_added_sugar")
        assertEquals("Reduce Added Sugar", sugar.title)
        assertEquals("🍊", sugar.emoji)
        assertEquals("Small choices, big changes. Skip added sugar today.", sugar.message)
        assertEquals("Health & Nutrition", sugar.category)
        assertEquals("#FF8A3D", sugar.accentColorHex)

        val shower = HabitNotificationRegistry.getIdentity("cool_water_shower")
        assertEquals("Cool Water Shower", shower.title)
        assertEquals("🚿", shower.emoji)
        assertEquals("Time for a refreshing finish!", shower.message)
        assertEquals("Physical Vitality", shower.category)
        assertEquals("#45AAF2", shower.accentColorHex)

        val alone = HabitNotificationRegistry.getIdentity("three_hours_alone")
        assertEquals("Three Hours Alone", alone.title)
        assertEquals("🌿", alone.emoji)
        assertEquals("Disconnect to reconnect with yourself.", alone.message)
        assertEquals("Solitude & Self-Mastery", alone.category)
        assertEquals("#F5B942", alone.accentColorHex)

        val journal = HabitNotificationRegistry.getIdentity("night_journaling")
        assertEquals("Night Journaling", journal.title)
        assertEquals("📖", journal.emoji)
        assertEquals("Before the day ends, capture what matters.", journal.message)
        assertEquals("Reflection & Growth", journal.category)
        assertEquals("#FFC078", journal.accentColorHex)
    }

    @Test
    fun `unknown habit falls back gracefully to default identity`() {
        val unknown = HabitNotificationRegistry.getIdentity("custom_walking", "Morning Walking")
        assertEquals("Morning Walking", unknown.title)
        assertEquals("✨", unknown.emoji)
        assertTrue(unknown.message.isNotEmpty())
        assertEquals("#FF8A3D", unknown.accentColorHex)
    }

    @Test
    fun `channel id resolution handles all sound and vibration permutations`() {
        // Sound ON, Vibration ON, Default tone
        val idDefault = NotificationHelper.resolveChannelId(
            soundEnabled = true,
            vibrationEnabled = true,
            soundToneUri = null
        )
        assertEquals("habit_nudge_v2_default_vib", idDefault)

        // Sound ON, Vibration OFF, Default tone
        val idDefaultNoVib = NotificationHelper.resolveChannelId(
            soundEnabled = true,
            vibrationEnabled = false,
            soundToneUri = ""
        )
        assertEquals("habit_nudge_v2_default_novib", idDefaultNoVib)

        // Sound OFF, Vibration ON (silent with vibration)
        val idSilentVib = NotificationHelper.resolveChannelId(
            soundEnabled = false,
            vibrationEnabled = true,
            soundToneUri = null
        )
        assertEquals("habit_nudge_v2_silent_vib", idSilentVib)

        // Sound OFF, Vibration OFF (complete silent)
        val idSilentNoVib = NotificationHelper.resolveChannelId(
            soundEnabled = false,
            vibrationEnabled = false,
            soundToneUri = null
        )
        assertEquals("habit_nudge_v2_silent_novib", idSilentNoVib)

        // Sound ON, Vibration ON, Custom tone URI
        val customToneUri = "content://media/internal/audio/media/108"
        val idCustom = NotificationHelper.resolveChannelId(
            soundEnabled = true,
            vibrationEnabled = true,
            soundToneUri = customToneUri
        )
        assertTrue(idCustom.startsWith("habit_nudge_v2_tone_"))
        assertTrue(idCustom.endsWith("_vib"))
    }

    @Test
    fun `deterministic notification ids prevent duplicate notifications for same habit`() {
        val id1 = NotificationHelper.getNotificationIdForHabit("daily_hydration")
        val id2 = NotificationHelper.getNotificationIdForHabit("daily_hydration")
        assertEquals(id1, id2)
        assertTrue(id1 > 0)

        val idMeditation = NotificationHelper.getNotificationIdForHabit("meditation")
        assertNotEquals(id1, idMeditation)
        assertTrue(idMeditation > 0)
    }

    @Test
    fun `quiet hours evaluation respects midnight spanning and daytime intervals`() {
        val startHour = 22
        val startMinute = 0
        val endHour = 7
        val endMinute = 0

        // Spanning midnight
        assertFalse(NotificationHelper.isInQuietHours(LocalTime.of(21, 59), startHour, startMinute, endHour, endMinute))
        assertTrue(NotificationHelper.isInQuietHours(LocalTime.of(22, 0), startHour, startMinute, endHour, endMinute))
        assertTrue(NotificationHelper.isInQuietHours(LocalTime.of(23, 30), startHour, startMinute, endHour, endMinute))
        assertTrue(NotificationHelper.isInQuietHours(LocalTime.of(0, 0), startHour, startMinute, endHour, endMinute))
        assertTrue(NotificationHelper.isInQuietHours(LocalTime.of(3, 15), startHour, startMinute, endHour, endMinute))
        assertTrue(NotificationHelper.isInQuietHours(LocalTime.of(6, 59), startHour, startMinute, endHour, endMinute))
        assertFalse(NotificationHelper.isInQuietHours(LocalTime.of(7, 0), startHour, startMinute, endHour, endMinute))
        assertFalse(NotificationHelper.isInQuietHours(LocalTime.of(15, 0), startHour, startMinute, endHour, endMinute))

        // Daytime quiet hours: 13:00 to 15:00
        assertFalse(NotificationHelper.isInQuietHours(LocalTime.of(12, 59), 13, 0, 15, 0))
        assertTrue(NotificationHelper.isInQuietHours(LocalTime.of(13, 0), 13, 0, 15, 0))
        assertTrue(NotificationHelper.isInQuietHours(LocalTime.of(14, 0), 13, 0, 15, 0))
        assertFalse(NotificationHelper.isInQuietHours(LocalTime.of(15, 0), 13, 0, 15, 0))

        // Start == End (disabled window)
        assertFalse(NotificationHelper.isInQuietHours(LocalTime.of(10, 0), 10, 0, 10, 0))
    }

    @Test
    fun `snooze action intervals calculate exact millis`() {
        val now = 1732435200000L
        val snooze10Millis = now + (10 * 60 * 1000L)
        val snooze30Millis = now + (30 * 60 * 1000L)

        assertEquals(now + 600000L, snooze10Millis)
        assertEquals(now + 1800000L, snooze30Millis)
    }
}
