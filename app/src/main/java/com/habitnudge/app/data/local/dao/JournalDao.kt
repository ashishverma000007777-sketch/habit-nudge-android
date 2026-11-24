package com.habitnudge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitnudge.app.data.local.entity.NightJournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM night_journals ORDER BY date DESC")
    fun getAllEntries(): Flow<List<NightJournalEntity>>

    @Query("SELECT * FROM night_journals WHERE date = :date LIMIT 1")
    fun getEntryForDate(date: String): Flow<NightJournalEntity?>

    @Query("SELECT * FROM night_journals WHERE date = :date LIMIT 1")
    suspend fun getEntryForDateDirect(date: String): NightJournalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: NightJournalEntity): Long

    @Query("DELETE FROM night_journals WHERE date = :date")
    suspend fun deleteForDate(date: String)
}
