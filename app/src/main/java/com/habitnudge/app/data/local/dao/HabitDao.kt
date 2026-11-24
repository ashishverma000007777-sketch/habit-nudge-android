package com.habitnudge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitnudge.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY orderIndex ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY orderIndex ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun observeHabitById(id: String): Flow<HabitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("UPDATE habits SET isActive = :isActive, startDate = :startDate WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean, startDate: Long?)

    @Query("UPDATE habits SET isPaused = :isPaused WHERE id = :id")
    suspend fun updatePausedStatus(id: String, isPaused: Boolean)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitsCount(): Int
}
