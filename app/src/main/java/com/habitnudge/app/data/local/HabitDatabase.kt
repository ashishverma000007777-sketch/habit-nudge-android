package com.habitnudge.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habitnudge.app.data.local.dao.CheckInDao
import com.habitnudge.app.data.local.dao.HabitDao
import com.habitnudge.app.data.local.dao.JournalDao
import com.habitnudge.app.data.local.dao.ReminderDao
import com.habitnudge.app.data.local.entity.FrequencyType
import com.habitnudge.app.data.local.entity.HabitCheckInEntity
import com.habitnudge.app.data.local.entity.HabitEntity
import com.habitnudge.app.data.local.entity.NightJournalEntity
import com.habitnudge.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@Database(
    entities = [
        HabitEntity::class,
        HabitCheckInEntity::class,
        ReminderEntity::class,
        NightJournalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao
    abstract fun reminderDao(): ReminderDao
    abstract fun journalDao(): JournalDao

    companion object {
        const val DATABASE_NAME = "habit_nudge.db"

        fun create(context: Context, scope: CoroutineScope): HabitDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                HabitDatabase::class.java,
                DATABASE_NAME
            ).addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch(Dispatchers.IO) {
                        seedInitialData(context)
                    }
                }
            }).build()
        }

        suspend fun seedInitialData(context: Context) {
            val db = create(context, CoroutineScope(Dispatchers.IO))
            val habitDao = db.habitDao()
            val reminderDao = db.reminderDao()

            if (habitDao.getHabitsCount() > 0) return

            val seedHabits = listOf(
                HabitEntity(
                    id = "reduce_added_sugar",
                    title = "Reduce Added Sugar",
                    tagline = "Clear mind & steady metabolic energy",
                    description = "Take control of your daily fuel by reducing processed sugar and sweeteners while keeping nourishing whole fruits.",
                    durationDays = 30,
                    frequency = FrequencyType.DAILY,
                    targetCountPerPeriod = 1,
                    tasksJson = JSONArray(listOf(
                        "Replace sugary drinks with water",
                        "Reduce candy and packaged sugary snacks",
                        "Check added sugar in regular foods",
                        "Do not unnecessarily avoid natural fruit"
                    )).toString(),
                    isOptional = false,
                    isActive = false,
                    category = "Health & Nutrition",
                    accentColorHex = "#FF8A3D",
                    orderIndex = 1
                ),
                HabitEntity(
                    id = "daily_hydration",
                    title = "Daily Hydration",
                    tagline = "Optimal alertness & cellular vitality",
                    description = "Maintain mindful hydration throughout the day based on natural thirst, current weather, and exertion.",
                    durationDays = 21,
                    frequency = FrequencyType.DAILY,
                    targetCountPerPeriod = 1,
                    tasksJson = JSONArray(listOf(
                        "Keep water nearby",
                        "Drink when thirsty",
                        "Adjust according to activity, weather and sweating",
                        "Avoid forcing excessive water consumption"
                    )).toString(),
                    isOptional = false,
                    isActive = false,
                    category = "Health & Nutrition",
                    accentColorHex = "#35C98A",
                    orderIndex = 2
                ),
                HabitEntity(
                    id = "no_phone_before_bed",
                    title = "No Phone Before Bed",
                    tagline = "Restorative deep sleep & peaceful evening",
                    description = "Protect your brain's melatonin rhythm by cutting blue light and digital dopamine an hour before sleep.",
                    durationDays = 14,
                    frequency = FrequencyType.DAILY,
                    targetCountPerPeriod = 1,
                    tasksJson = JSONArray(listOf(
                        "Put the phone away one hour before bed",
                        "Avoid scrolling, videos and unnecessary messages",
                        "Read a few pages or prepare for tomorrow"
                    )).toString(),
                    isOptional = false,
                    isActive = false,
                    category = "Digital Wellbeing & Sleep",
                    accentColorHex = "#8E7CFF",
                    orderIndex = 3
                ),
                HabitEntity(
                    id = "meditation",
                    title = "Meditation",
                    tagline = "Quiet stillness & emotional clarity",
                    description = "A simple 10-minute quiet pause to anchor your attention on breathing and cultivate inner stillness.",
                    durationDays = 21,
                    frequency = FrequencyType.DAILY,
                    targetCountPerPeriod = 1,
                    tasksJson = JSONArray(listOf(
                        "Meditate for 10 minutes",
                        "Sit somewhere quiet",
                        "Focus on breathing",
                        "Return attention gently whenever distracted"
                    )).toString(),
                    isOptional = false,
                    isActive = false,
                    category = "Mindfulness & Focus",
                    accentColorHex = "#4ECDC4",
                    orderIndex = 4
                ),
                HabitEntity(
                    id = "cool_water_shower",
                    title = "Cool Water Shower",
                    tagline = "Gentle invigoration & thermal resilience",
                    description = "Conclude your standard shower with a gentle rinse of mildly cool water to stimulate circulation and calm the nervous system.",
                    durationDays = 30,
                    frequency = FrequencyType.DAILY,
                    targetCountPerPeriod = 1,
                    tasksJson = JSONArray(listOf(
                        "Finish a normal shower with mildly cool water",
                        "Adjust gradually",
                        "Stop if feeling unwell",
                        "Never force extreme cold exposure"
                    )).toString(),
                    isOptional = true,
                    isActive = false,
                    category = "Physical Vitality",
                    accentColorHex = "#45AAF2",
                    orderIndex = 5
                ),
                HabitEntity(
                    id = "three_hours_alone",
                    title = "Three Hours Alone",
                    tagline = "Intentional solitude & deep reflection",
                    description = "Dedicate three undisturbed hours each week disconnected from social feeds to recharge your mental compass.",
                    durationDays = 28,
                    frequency = FrequencyType.WEEKLY,
                    targetCountPerPeriod = 1,
                    tasksJson = JSONArray(listOf(
                        "Spend three hours alone",
                        "Avoid social media and constant messages",
                        "Walk, visit a park, sit in a cafe or enjoy a solo activity",
                        "Reflect on personal goals and daily routine"
                    )).toString(),
                    isOptional = false,
                    isActive = false,
                    category = "Solitude & Self-Mastery",
                    accentColorHex = "#F5B942",
                    orderIndex = 6
                ),
                HabitEntity(
                    id = "night_journaling",
                    title = "Night Journaling",
                    tagline = "Nightly closure & thoughtful intentions",
                    description = "Spend 10 reflective minutes before sleep answering three essential prompts to close the day with clarity.",
                    durationDays = 30,
                    frequency = FrequencyType.DAILY,
                    targetCountPerPeriod = 1,
                    tasksJson = JSONArray(listOf(
                        "Write for 10 minutes before bed",
                        "What went well today?",
                        "What wasted my time or energy?",
                        "What should I improve tomorrow?"
                    )).toString(),
                    isOptional = false,
                    isActive = false,
                    category = "Reflection & Growth",
                    accentColorHex = "#FFC078",
                    orderIndex = 7
                )
            )

            habitDao.insertAll(seedHabits)

            // Seed default reminder presets
            val seedReminders = listOf(
                // Hydration: 10am, 12pm, 2pm, 4pm, 6pm
                ReminderEntity(habitId = "daily_hydration", timeHour = 10, timeMinute = 0, isEnabled = true, label = "Mid-morning water check"),
                ReminderEntity(habitId = "daily_hydration", timeHour = 12, timeMinute = 0, isEnabled = true, label = "Lunch hydration"),
                ReminderEntity(habitId = "daily_hydration", timeHour = 14, timeMinute = 0, isEnabled = true, label = "Afternoon energy sip"),
                ReminderEntity(habitId = "daily_hydration", timeHour = 16, timeMinute = 0, isEnabled = true, label = "Late-afternoon glass"),
                ReminderEntity(habitId = "daily_hydration", timeHour = 18, timeMinute = 0, isEnabled = true, label = "Evening hydration"),
                // Meditation: 9am
                ReminderEntity(habitId = "meditation", timeHour = 9, timeMinute = 0, isEnabled = true, label = "Morning 10-minute stillness"),
                // Reduce Added Sugar: 8:30am
                ReminderEntity(habitId = "reduce_added_sugar", timeHour = 8, timeMinute = 30, isEnabled = true, label = "Mindful breakfast choices"),
                // No Phone: 10pm
                ReminderEntity(habitId = "no_phone_before_bed", timeHour = 22, timeMinute = 0, isEnabled = true, label = "Screen shutdown time"),
                // Cool Shower: 7:30am
                ReminderEntity(habitId = "cool_water_shower", timeHour = 7, timeMinute = 30, isEnabled = true, label = "Morning invigorating rinse"),
                // Three Hours Alone: Saturday 10:00 AM (repeatDay 6 = Saturday)
                ReminderEntity(habitId = "three_hours_alone", timeHour = 10, timeMinute = 0, isEnabled = true, repeatDaysCsv = "6", label = "Weekly solo recharge"),
                // Night Journal: 11pm
                ReminderEntity(habitId = "night_journaling", timeHour = 23, timeMinute = 0, isEnabled = true, label = "Evening 3-prompt reflection")
            )

            reminderDao.insertAll(seedReminders)
        }
    }
}
