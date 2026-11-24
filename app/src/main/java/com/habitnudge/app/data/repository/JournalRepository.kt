package com.habitnudge.app.data.repository

import com.habitnudge.app.data.local.dao.JournalDao
import com.habitnudge.app.data.local.entity.NightJournalEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getAllEntries(): Flow<List<NightJournalEntity>> = journalDao.getAllEntries()

    fun getEntryForDate(date: String): Flow<NightJournalEntity?> = journalDao.getEntryForDate(date)

    suspend fun getTodayEntry(): NightJournalEntity? {
        val todayStr = LocalDate.now().format(dateFormatter)
        return journalDao.getEntryForDateDirect(todayStr)
    }

    suspend fun saveEntry(
        date: String,
        wentWell: String,
        wastedTimeOrEnergy: String,
        improveTomorrow: String
    ): Long {
        val existing = journalDao.getEntryForDateDirect(date)
        val entry = NightJournalEntity(
            id = existing?.id ?: 0,
            date = date,
            timestamp = System.currentTimeMillis(),
            wentWell = wentWell,
            wastedTimeOrEnergy = wastedTimeOrEnergy,
            improveTomorrow = improveTomorrow
        )
        return journalDao.insertOrUpdate(entry)
    }

    suspend fun deleteForDate(date: String) {
        journalDao.deleteForDate(date)
    }
}
