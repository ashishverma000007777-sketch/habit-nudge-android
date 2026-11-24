package com.habitnudge.app.data.repository

import com.habitnudge.app.data.local.dao.ReminderDao
import com.habitnudge.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {
    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    fun getEnabledReminders(): Flow<List<ReminderEntity>> = reminderDao.getEnabledReminders()

    suspend fun getEnabledRemindersList(): List<ReminderEntity> = reminderDao.getEnabledRemindersList()

    fun getRemindersForHabit(habitId: String): Flow<List<ReminderEntity>> = reminderDao.getRemindersForHabit(habitId)

    suspend fun getReminderById(id: Long): ReminderEntity? = reminderDao.getReminderById(id)

    suspend fun addReminder(
        habitId: String,
        timeHour: Int,
        timeMinute: Int,
        repeatDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
        label: String? = null
    ): Long {
        val entity = ReminderEntity(
            habitId = habitId,
            timeHour = timeHour,
            timeMinute = timeMinute,
            repeatDaysCsv = repeatDays.joinToString(","),
            label = label,
            isEnabled = true
        )
        return reminderDao.insert(entity)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.update(reminder)
    }

    suspend fun deleteReminder(id: Long) {
        reminderDao.deleteById(id)
    }

    suspend fun toggleReminder(id: Long, enabled: Boolean) {
        reminderDao.setEnabled(id, enabled)
    }

    suspend fun snoozeReminder(id: Long, minutes: Int) {
        val snoozeUntil = System.currentTimeMillis() + (minutes * 60 * 1000L)
        reminderDao.updateSnooze(id, snoozeUntil)
    }

    suspend fun clearSnooze(id: Long) {
        reminderDao.updateSnooze(id, null)
    }
}
