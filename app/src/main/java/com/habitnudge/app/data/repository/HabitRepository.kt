package com.habitnudge.app.data.repository

import android.content.Context
import com.habitnudge.app.data.local.HabitDatabase
import com.habitnudge.app.data.local.dao.CheckInDao
import com.habitnudge.app.data.local.dao.HabitDao
import com.habitnudge.app.data.local.dao.ReminderDao
import com.habitnudge.app.data.local.entity.HabitCheckInEntity
import com.habitnudge.app.data.local.entity.HabitEntity
import com.habitnudge.app.domain.model.DayCompletion
import com.habitnudge.app.domain.model.Habit
import com.habitnudge.app.domain.model.HabitWithProgress
import com.habitnudge.app.domain.model.OverallDailyStats
import com.habitnudge.app.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao,
    private val reminderDao: ReminderDao,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun ensureInitialized() {
        if (habitDao.getHabitsCount() == 0) {
            HabitDatabase.seedInitialData(context)
        }
    }

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits().combine(checkInDao.getAllCheckIns()) { entities, _ ->
        entities.map { it.toDomain() }
    }

    fun getActiveHabitsWithProgress(): Flow<List<HabitWithProgress>> {
        return combine(
            habitDao.getActiveHabits(),
            checkInDao.getAllCheckIns(),
            reminderDao.getAllReminders()
        ) { habits, allCheckIns, reminders ->
            val today = LocalDate.now()
            habits.map { entity ->
                val habitCheckIns = allCheckIns.filter { it.habitId == entity.id }
                val progress = calculateStreakUseCase.execute(
                    startDateMillis = entity.startDate,
                    durationDays = entity.durationDays,
                    checkIns = habitCheckIns,
                    today = today
                )
                val nextReminder = reminders
                    .filter { it.habitId == entity.id && it.isEnabled }
                    .minByOrNull { it.timeHour * 60 + it.timeMinute }
                    ?.let { String.format(Locale.US, "%02d:%02d", it.timeHour, it.timeMinute) }

                HabitWithProgress(
                    habit = entity.toDomain(),
                    progress = progress,
                    nextReminder = nextReminder
                )
            }
        }
    }

    fun getHabitWithProgress(habitId: String): Flow<HabitWithProgress?> {
        return combine(
            habitDao.observeHabitById(habitId),
            checkInDao.getCheckInsForHabit(habitId),
            reminderDao.getRemindersForHabit(habitId)
        ) { entity, checkIns, reminders ->
            if (entity == null) return@combine null
            val today = LocalDate.now()
            val progress = calculateStreakUseCase.execute(
                startDateMillis = entity.startDate,
                durationDays = entity.durationDays,
                checkIns = checkIns,
                today = today
            )
            val nextReminder = reminders
                .filter { it.isEnabled }
                .minByOrNull { it.timeHour * 60 + it.timeMinute }
                ?.let { String.format(Locale.US, "%02d:%02d", it.timeHour, it.timeMinute) }

            HabitWithProgress(
                habit = entity.toDomain(),
                progress = progress,
                nextReminder = nextReminder
            )
        }
    }

    fun getCheckInsForHabit(habitId: String): Flow<List<HabitCheckInEntity>> {
        return checkInDao.getCheckInsForHabit(habitId)
    }

    suspend fun setHabitActive(habitId: String, active: Boolean) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val startDate = if (active) {
            habit.startDate ?: System.currentTimeMillis()
        } else {
            habit.startDate
        }
        habitDao.updateActiveStatus(habitId, active, startDate)
    }

    suspend fun setHabitPaused(habitId: String, paused: Boolean) {
        habitDao.updatePausedStatus(habitId, paused)
    }

    suspend fun restartHabit(habitId: String) {
        checkInDao.clearHistoryForHabit(habitId)
        habitDao.updateActiveStatus(habitId, true, System.currentTimeMillis())
        habitDao.updatePausedStatus(habitId, false)
    }

    suspend fun toggleCheckInToday(habitId: String): Boolean {
        val todayStr = LocalDate.now().format(dateFormatter)
        val existing = checkInDao.getCheckIn(habitId, todayStr)
        return if (existing != null && existing.completed) {
            checkInDao.deleteCheckIn(habitId, todayStr)
            false
        } else {
            checkInDao.insertOrUpdate(
                HabitCheckInEntity(
                    habitId = habitId,
                    date = todayStr,
                    timestamp = System.currentTimeMillis(),
                    completed = true
                )
            )
            true
        }
    }

    suspend fun markHabitCompletedForToday(habitId: String) {
        val todayStr = LocalDate.now().format(dateFormatter)
        checkInDao.insertOrUpdate(
            HabitCheckInEntity(
                habitId = habitId,
                date = todayStr,
                timestamp = System.currentTimeMillis(),
                completed = true
            )
        )
    }

    fun getWeeklyActivity(habitId: String? = null): Flow<List<DayCompletion>> {
        return checkInDao.getAllCheckIns().combine(habitDao.getActiveHabits()) { checkIns, activeHabits ->
            val today = LocalDate.now()
            // Find start of week (Monday)
            val monday = today.with(DayOfWeek.MONDAY)
            (0..6).map { offset ->
                val date = monday.plusDays(offset.toLong())
                val dateStr = date.format(dateFormatter)
                val isCompleted = if (habitId != null) {
                    checkIns.any { it.habitId == habitId && it.date == dateStr && it.completed }
                } else {
                    // Overall: completed if any active habit was completed on that date, or all if habits exist
                    val activeIds = activeHabits.map { it.id }.toSet()
                    if (activeIds.isEmpty()) false
                    else checkIns.any { activeIds.contains(it.habitId) && it.date == dateStr && it.completed }
                }
                val letter = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                DayCompletion(
                    date = date,
                    dayOfWeekLetter = letter,
                    isCompleted = isCompleted,
                    isToday = date == today,
                    isFuture = date.isAfter(today)
                )
            }
        }
    }

    fun getOverallDailyStats(): Flow<OverallDailyStats> {
        return combine(
            habitDao.getActiveHabits(),
            checkInDao.getAllCheckIns()
        ) { activeHabits, allCheckIns ->
            val today = LocalDate.now()
            val todayStr = today.format(dateFormatter)
            val totalActive = activeHabits.size

            if (totalActive == 0) {
                return@combine OverallDailyStats(
                    totalActiveHabits = 0,
                    completedTodayCount = 0,
                    completionPercentage = 0,
                    activeStreaksSum = 0,
                    bestOverallStreak = 0,
                    completedChallengesCount = 0
                )
            }

            val completedTodayCount = activeHabits.count { habit ->
                allCheckIns.any { it.habitId == habit.id && it.date == todayStr && it.completed }
            }

            val percentage = ((completedTodayCount.toFloat() / totalActive) * 100).toInt()

            var streaksSum = 0
            var bestStreak = 0
            var completedChallenges = 0

            activeHabits.forEach { habit ->
                val habitCheckIns = allCheckIns.filter { it.habitId == habit.id }
                val progress = calculateStreakUseCase.execute(
                    startDateMillis = habit.startDate,
                    durationDays = habit.durationDays,
                    checkIns = habitCheckIns,
                    today = today
                )
                streaksSum += progress.currentStreak
                if (progress.bestStreak > bestStreak) {
                    bestStreak = progress.bestStreak
                }
                if (progress.isChallengeComplete) {
                    completedChallenges++
                }
            }

            OverallDailyStats(
                totalActiveHabits = totalActive,
                completedTodayCount = completedTodayCount,
                completionPercentage = percentage,
                activeStreaksSum = streaksSum,
                bestOverallStreak = bestStreak,
                completedChallengesCount = completedChallenges
            )
        }
    }

    private fun HabitEntity.toDomain(): Habit {
        val taskList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(tasksJson)
            for (i in 0 until jsonArray.length()) {
                taskList.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // fallback
        }
        return Habit(
            id = id,
            title = title,
            tagline = tagline,
            description = description,
            durationDays = durationDays,
            frequency = frequency,
            targetCountPerPeriod = targetCountPerPeriod,
            tasks = taskList,
            isOptional = isOptional,
            isActive = isActive,
            startDate = startDate,
            isPaused = isPaused,
            category = category,
            accentColorHex = accentColorHex,
            orderIndex = orderIndex
        )
    }
}
