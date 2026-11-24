package com.habitnudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: String,
    val timeHour: Int, // 0-23
    val timeMinute: Int, // 0-59
    val isEnabled: Boolean = true,
    val repeatDaysCsv: String = "1,2,3,4,5,6,7", // 1=Mon .. 7=Sun
    val snoozeUntilMillis: Long? = null,
    val label: String? = null
)
