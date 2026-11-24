package com.habitnudge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitnudge.app.data.local.entity.HabitCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM habit_checkins WHERE date = :date")
    fun getCheckInsForDate(date: String): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId ORDER BY date DESC")
    fun getCheckInsForHabit(habitId: String): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getCheckInsListForHabit(habitId: String): List<HabitCheckInEntity>

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCheckIn(habitId: String, date: String): HabitCheckInEntity?

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId AND date = :date LIMIT 1")
    fun observeCheckIn(habitId: String, date: String): Flow<HabitCheckInEntity?>

    @Query("SELECT * FROM habit_checkins")
    fun getAllCheckIns(): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_checkins")
    suspend fun getAllCheckInsList(): List<HabitCheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(checkIn: HabitCheckInEntity)

    @Query("DELETE FROM habit_checkins WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCheckIn(habitId: String, date: String)

    @Query("DELETE FROM habit_checkins WHERE habitId = :habitId")
    suspend fun clearHistoryForHabit(habitId: String)
}
