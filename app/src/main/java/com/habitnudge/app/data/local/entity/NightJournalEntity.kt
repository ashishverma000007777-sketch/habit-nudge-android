package com.habitnudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "night_journals")
data class NightJournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val wentWell: String,
    val wastedTimeOrEnergy: String,
    val improveTomorrow: String
)
