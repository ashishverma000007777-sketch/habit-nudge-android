package com.habitnudge.app.domain.usecase

import com.habitnudge.app.data.local.entity.HabitCheckInEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalculateStreakUseCaseTest {

    private lateinit var useCase: CalculateStreakUseCase
    private val today = LocalDate.of(2026, 11, 24)

    @Before
    fun setUp() {
        useCase = CalculateStreakUseCase()
    }

    @Test
    fun `empty check-ins returns zero streak and zero progress`() {
        val result = useCase.execute(
            startDateMillis = null,
            durationDays = 30,
            checkIns = emptyList(),
            today = today
        )

        assertEquals(0, result.currentStreak)
        assertEquals(0, result.bestStreak)
        assertEquals(0, result.completedDaysCount)
        assertEquals(0f, result.progressFraction, 0.001f)
        assertFalse(result.isCompletedToday)
        assertFalse(result.isChallengeComplete)
        assertEquals(30, result.remainingDays)
    }

    @Test
    fun `check-in today gives streak of 1`() {
        val checkIns = listOf(
            HabitCheckInEntity(habitId = "daily_hydration", date = "2026-11-24", completed = true)
        )

        val result = useCase.execute(
            startDateMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            durationDays = 21,
            checkIns = checkIns,
            today = today
        )

        assertEquals(1, result.currentStreak)
        assertEquals(1, result.bestStreak)
        assertEquals(1, result.completedDaysCount)
        assertTrue(result.isCompletedToday)
        assertEquals(20, result.remainingDays)
    }

    @Test
    fun `consecutive 5 days ending yesterday maintains streak of 5 before today is checked`() {
        val checkIns = listOf(
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-23", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-22", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-21", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-20", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-19", completed = true)
        )

        val result = useCase.execute(
            startDateMillis = LocalDate.of(2026, 11, 19).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            durationDays = 30,
            checkIns = checkIns,
            today = today
        )

        assertEquals(5, result.currentStreak)
        assertEquals(5, result.bestStreak)
        assertEquals(5, result.completedDaysCount)
        assertFalse(result.isCompletedToday)
    }

    @Test
    fun `checking in today increases streak from 5 to 6`() {
        val checkIns = listOf(
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-24", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-23", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-22", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-21", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-20", completed = true),
            HabitCheckInEntity(habitId = "reduce_added_sugar", date = "2026-11-19", completed = true)
        )

        val result = useCase.execute(
            startDateMillis = LocalDate.of(2026, 11, 19).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            durationDays = 30,
            checkIns = checkIns,
            today = today
        )

        assertEquals(6, result.currentStreak)
        assertEquals(6, result.bestStreak)
        assertEquals(6, result.completedDaysCount)
        assertTrue(result.isCompletedToday)
    }

    @Test
    fun `broken streak resets current streak to 0 if yesterday was missed and today is not checked`() {
        // Checked in 3 days ago, but missed yesterday and day before
        val checkIns = listOf(
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-21", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-20", completed = true),
            HabitCheckInEntity(habitId = "meditation", date = "2026-11-19", completed = true)
        )

        val result = useCase.execute(
            startDateMillis = LocalDate.of(2026, 11, 19).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            durationDays = 21,
            checkIns = checkIns,
            today = today
        )

        assertEquals(0, result.currentStreak) // Reset because yesterday was missed
        assertEquals(3, result.bestStreak) // Best streak retained!
        assertEquals(3, result.completedDaysCount)
        assertEquals(2, result.missedDaysCount) // 22nd and 23rd were missed
    }

    @Test
    fun `challenge completion is detected when completed days reach duration`() {
        val checkIns = (1..14).map { day ->
            val dateStr = String.format("2026-11-%02d", day)
            HabitCheckInEntity(habitId = "no_phone_before_bed", date = dateStr, completed = true)
        }

        val result = useCase.execute(
            startDateMillis = LocalDate.of(2026, 11, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            durationDays = 14,
            checkIns = checkIns,
            today = today
        )

        assertTrue(result.isChallengeComplete)
        assertEquals(1.0f, result.progressFraction, 0.001f)
        assertEquals(0, result.remainingDays)
    }
}
