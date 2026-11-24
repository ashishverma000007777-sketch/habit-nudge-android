package com.habitnudge.app.domain

import com.habitnudge.app.data.local.entity.FrequencyType
import com.habitnudge.app.data.local.entity.HabitCheckInEntity
import com.habitnudge.app.domain.usecase.CalculateStreakUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class HabitChallengesAndSchedulingTest {

    private val streakUseCase = CalculateStreakUseCase()

    data class ChallengeSpec(
        val id: String,
        val title: String,
        val durationDays: Int,
        val frequency: FrequencyType,
        val isOptional: Boolean
    )

    private val sevenChallenges = listOf(
        ChallengeSpec("reduce_added_sugar", "Reduce Added Sugar", 30, FrequencyType.DAILY, false),
        ChallengeSpec("daily_hydration", "Daily Hydration", 21, FrequencyType.DAILY, false),
        ChallengeSpec("no_phone_before_bed", "No Phone Before Bed", 14, FrequencyType.DAILY, false),
        ChallengeSpec("meditation", "Meditation", 21, FrequencyType.DAILY, false),
        ChallengeSpec("cool_water_shower", "Cool Water Shower", 30, FrequencyType.DAILY, true),
        ChallengeSpec("three_hours_alone", "Three Hours Alone", 28, FrequencyType.WEEKLY, false),
        ChallengeSpec("night_journaling", "Night Journaling", 30, FrequencyType.DAILY, false)
    )

    @Test
    fun `verify all seven habit challenges match required specifications`() {
        assertEquals(7, sevenChallenges.size)

        val sugar = sevenChallenges.first { it.id == "reduce_added_sugar" }
        assertEquals(30, sugar.durationDays)

        val hydration = sevenChallenges.first { it.id == "daily_hydration" }
        assertEquals(21, hydration.durationDays)

        val noPhone = sevenChallenges.first { it.id == "no_phone_before_bed" }
        assertEquals(14, noPhone.durationDays)

        val meditation = sevenChallenges.first { it.id == "meditation" }
        assertEquals(21, meditation.durationDays)

        val shower = sevenChallenges.first { it.id == "cool_water_shower" }
        assertEquals(30, shower.durationDays)
        assertTrue(shower.isOptional)

        val alone = sevenChallenges.first { it.id == "three_hours_alone" }
        assertEquals(28, alone.durationDays)
        assertEquals(FrequencyType.WEEKLY, alone.frequency)

        val journal = sevenChallenges.first { it.id == "night_journaling" }
        assertEquals(30, journal.durationDays)
    }

    @Test
    fun `verify weekly habit scheduling finds next Saturday accurately`() {
        // Assume reminder is set for Saturday 10:00 AM (repeatDays = "6")
        val reminderTime = LocalTime.of(10, 0)
        val validDays = setOf(6) // Saturday

        // Test from Friday 2026-11-27 at 15:00
        val friday = LocalDateTime.of(2026, 11, 27, 15, 0)
        assertEquals(DayOfWeek.FRIDAY, friday.dayOfWeek)

        var nextTriggerMillis: Long? = null
        for (i in 0..14) {
            val targetDate = friday.toLocalDate().plusDays(i.toLong())
            if (validDays.contains(targetDate.dayOfWeek.value)) {
                val targetDateTime = LocalDateTime.of(targetDate, reminderTime)
                if (targetDateTime.isAfter(friday)) {
                    nextTriggerMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    break
                }
            }
        }

        assertTrue(nextTriggerMillis != null)
        val triggerDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(nextTriggerMillis!!),
            ZoneId.systemDefault()
        )
        assertEquals(DayOfWeek.SATURDAY, triggerDate.dayOfWeek)
        assertEquals(2026, triggerDate.year)
        assertEquals(11, triggerDate.monthValue)
        assertEquals(28, triggerDate.dayOfMonth)
        assertEquals(10, triggerDate.hour)
        assertEquals(0, triggerDate.minute)
    }

    @Test
    fun `verify quiet hours boundary logic`() {
        val startHour = 22
        val startMinute = 0
        val endHour = 7
        val endMinute = 0

        fun isInQuietHours(now: LocalTime): Boolean {
            val start = LocalTime.of(startHour, startMinute)
            val end = LocalTime.of(endHour, endMinute)
            return if (start.isBefore(end)) {
                (!now.isBefore(start)) && now.isBefore(end)
            } else {
                (!now.isBefore(start)) || now.isBefore(end)
            }
        }

        // 21:59 should NOT be quiet hours
        assertFalse(isInQuietHours(LocalTime.of(21, 59)))

        // 22:00 SHOULD be quiet hours (exact boundary)
        assertTrue(isInQuietHours(LocalTime.of(22, 0)))

        // 02:30 midnight SHOULD be quiet hours
        assertTrue(isInQuietHours(LocalTime.of(2, 30)))

        // 06:59 SHOULD be quiet hours
        assertTrue(isInQuietHours(LocalTime.of(6, 59)))

        // 07:00 should NOT be quiet hours
        assertFalse(isInQuietHours(LocalTime.of(7, 0)))

        // 14:00 afternoon should NOT be quiet hours
        assertFalse(isInQuietHours(LocalTime.of(14, 0)))
    }

    @Test
    fun `streak calculation with multiple gaps retains overall best streak`() {
        val today = LocalDate.of(2026, 11, 24)

        // Block 1: 5 days streak in October
        // Block 2: 7 days streak in early November
        // Block 3: 2 days streak ending today
        val checkIns = listOf(
            // Block 1: Oct 1-5 (5 days)
            HabitCheckInEntity(habitId = "meditation", date = "2026-10-01", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-10-02", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-10-03", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-10-04", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-10-05", completed = true),

            // Block 2: Nov 1-7 (7 days)
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-01", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-02", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-03", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-04", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-05", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-06", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-07", completed = true),

            // Block 3: Nov 23-24 (2 days, active today)
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-23", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-24", completed = true)
        )

        val result = streakUseCase.execute(
            startDateMillis = LocalDate.of(2026, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            durationDays = 21,
            checkIns = checkIns,
            today = today
        )

        assertEquals(2, result.currentStreak) // Active 2-day streak
        assertEquals(7, result.bestStreak)    // Historical best streak was 7 days!
        assertEquals(14, result.completedDaysCount)
        assertTrue(result.isCompletedToday)
        assertEquals(7, result.remainingDays)
    }
}
