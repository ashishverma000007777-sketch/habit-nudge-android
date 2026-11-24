package com.habitnudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FrequencyType {
    DAILY,
    WEEKLY
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val tagline: String,
    val description: String,
    val durationDays: Int,
    val frequency: FrequencyType = FrequencyType.DAILY,
    val targetCountPerPeriod: Int = 1,
    val tasksJson: String, // JSON array of task strings
    val isOptional: Boolean = false,
    val isActive: Boolean = false,
    val startDate: Long? = null,
    val isPaused: Boolean = false,
    val category: String,
    val accentColorHex: String = "#FF8A3D",
    val orderIndex: Int = 0
)
