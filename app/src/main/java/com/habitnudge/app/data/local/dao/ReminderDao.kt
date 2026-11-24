package com.habitnudge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitnudge.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timeHour ASC, timeMinute ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY timeHour ASC, timeMinute ASC")
    fun getEnabledReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getEnabledRemindersList(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE habitId = :habitId ORDER BY timeHour ASC, timeMinute ASC")
    fun getRemindersForHabit(habitId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE habitId = :habitId")
    suspend fun getRemindersListForHabit(habitId: String): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderEntity>)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE reminders SET snoozeUntilMillis = :snoozeUntilMillis WHERE id = :id")
    suspend fun updateSnooze(id: Long, snoozeUntilMillis: Long?)

    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun getRemindersCount(): Int
}
