package com.habitnudge.app.data.local

import androidx.room.TypeConverter
import com.habitnudge.app.data.local.entity.FrequencyType

class Converters {
    @TypeConverter
    fun fromFrequencyType(value: FrequencyType): String = value.name

    @TypeConverter
    fun toFrequencyType(value: String): FrequencyType = try {
        FrequencyType.valueOf(value)
    } catch (e: Exception) {
        FrequencyType.DAILY
    }
}
