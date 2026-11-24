package com.habitnudge.app.domain.model

import com.habitnudge.app.data.local.entity.FrequencyType
import java.time.LocalDate

data class Habit(
    val id: String,
    val title: String,
    val tagline: String,
    val description: String,
    val durationDays: Int,
    val frequency: FrequencyType,
    val targetCountPerPeriod: Int,
    val tasks: List<String>,
    val isOptional: Boolean,
    val isActive: Boolean,
    val startDate: Long?,
    val isPaused: Boolean,
    val category: String,
    val accentColorHex: String,
    val orderIndex: Int
)

data class HabitWithProgress(
    val habit: Habit,
    val progress: HabitProgress,
    val nextReminder: String? = null
)

data class HabitProgress(
    val totalDaysTarget: Int,
    val completedDaysCount: Int,
    val progressFraction: Float,
    val currentStreak: Int,
    val bestStreak: Int,
    val missedDaysCount: Int,
    val isCompletedToday: Boolean,
    val isChallengeComplete: Boolean,
    val remainingDays: Int
)

data class DayCompletion(
    val date: LocalDate,
    val dayOfWeekLetter: String, // "M", "T", "W", "T", "F", "S", "S"
    val isCompleted: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class OverallDailyStats(
    val totalActiveHabits: Int,
    val completedTodayCount: Int,
    val completionPercentage: Int, // 0 to 100
    val activeStreaksSum: Int,
    val bestOverallStreak: Int,
    val completedChallengesCount: Int
)
