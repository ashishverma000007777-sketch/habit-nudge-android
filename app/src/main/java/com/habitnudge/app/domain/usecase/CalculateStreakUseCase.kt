package com.habitnudge.app.domain.usecase

import com.habitnudge.app.data.local.entity.HabitCheckInEntity
import com.habitnudge.app.domain.model.HabitProgress
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor() {

    fun execute(
        startDateMillis: Long?,
        durationDays: Int,
        checkIns: List<HabitCheckInEntity>,
        today: LocalDate = LocalDate.now()
    ): HabitProgress {
        val completedDates = checkIns
            .filter { it.completed }
            .mapNotNull { parseDate(it.date) }
            .toSet()

        val isCompletedToday = completedDates.contains(today)
        val completedDaysCount = completedDates.size

        // Streak calculation
        val currentStreak = calculateCurrentStreak(completedDates, today, isCompletedToday)
        val bestStreak = calculateBestStreak(completedDates)

        // Missed days calculation
        val missedDaysCount = if (startDateMillis != null) {
            val startDate = Instant.ofEpochMilli(startDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val daysSinceStart = ChronoUnit.DAYS.between(startDate, today).toInt()
            if (daysSinceStart > 0) {
                // Check each day between startDate and yesterday
                (0 until daysSinceStart).count { dayOffset ->
                    val checkDate = startDate.plusDays(dayOffset.toLong())
                    !completedDates.contains(checkDate)
                }
            } else 0
        } else 0

        val progressFraction = (completedDaysCount.toFloat() / durationDays.coerceAtLeast(1))
            .coerceIn(0f, 1f)
        val isChallengeComplete = completedDaysCount >= durationDays
        val remainingDays = (durationDays - completedDaysCount).coerceAtLeast(0)

        return HabitProgress(
            totalDaysTarget = durationDays,
            completedDaysCount = completedDaysCount,
            progressFraction = progressFraction,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            missedDaysCount = missedDaysCount,
            isCompletedToday = isCompletedToday,
            isChallengeComplete = isChallengeComplete,
            remainingDays = remainingDays
        )
    }

    private fun calculateCurrentStreak(
        completedDates: Set<LocalDate>,
        today: LocalDate,
        isCompletedToday: Boolean
    ): Int {
        var streak = 0
        var checkDate = if (isCompletedToday) today else today.minusDays(1)

        while (completedDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    private fun calculateBestStreak(completedDates: Set<LocalDate>): Int {
        if (completedDates.isEmpty()) return 0
        val sortedDates = completedDates.sorted()
        var maxStreak = 1
        var currentSequence = 1

        for (i in 1 until sortedDates.size) {
            val prev = sortedDates[i - 1]
            val curr = sortedDates[i]
            if (ChronoUnit.DAYS.between(prev, curr) == 1L) {
                currentSequence++
                if (currentSequence > maxStreak) {
                    maxStreak = currentSequence
                }
            } else if (ChronoUnit.DAYS.between(prev, curr) > 1L) {
                currentSequence = 1
            }
        }
        return maxStreak
    }

    private fun parseDate(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}
