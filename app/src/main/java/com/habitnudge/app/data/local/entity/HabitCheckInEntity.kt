package com.habitnudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_checkins",
    indices = [
        Index(value = ["habitId", "date"], unique = true)
    ]
)
data class HabitCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: String,
    val date: String, // Format: YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = true,
    val notes: String? = null
)
