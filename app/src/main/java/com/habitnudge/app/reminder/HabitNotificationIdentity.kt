package com.habitnudge.app.reminder

data class HabitNotificationIdentity(
    val habitId: String,
    val title: String,
    val emoji: String,
    val message: String,
    val category: String,
    val accentColorHex: String
)

object HabitNotificationRegistry {

    private val identities = mapOf(
        "daily_hydration" to HabitNotificationIdentity(
            habitId = "daily_hydration",
            title = "Daily Hydration",
            emoji = "💧",
            message = "Your body needs water. Take a mindful sip!",
            category = "Health & Nutrition",
            accentColorHex = "#35C98A"
        ),
        "meditation" to HabitNotificationIdentity(
            habitId = "meditation",
            title = "Meditation",
            emoji = "🧘",
            message = "Pause the noise. Give yourself 10 minutes.",
            category = "Mindfulness & Focus",
            accentColorHex = "#4ECDC4"
        ),
        "no_phone_before_bed" to HabitNotificationIdentity(
            habitId = "no_phone_before_bed",
            title = "No Phone Before Bed",
            emoji = "📵",
            message = "Your sleep deserves better. Put the phone away.",
            category = "Digital Wellbeing & Sleep",
            accentColorHex = "#8E7CFF"
        ),
        "reduce_added_sugar" to HabitNotificationIdentity(
            habitId = "reduce_added_sugar",
            title = "Reduce Added Sugar",
            emoji = "🍊",
            message = "Small choices, big changes. Skip added sugar today.",
            category = "Health & Nutrition",
            accentColorHex = "#FF8A3D"
        ),
        "cool_water_shower" to HabitNotificationIdentity(
            habitId = "cool_water_shower",
            title = "Cool Water Shower",
            emoji = "🚿",
            message = "Time for a refreshing finish!",
            category = "Physical Vitality",
            accentColorHex = "#45AAF2"
        ),
        "three_hours_alone" to HabitNotificationIdentity(
            habitId = "three_hours_alone",
            title = "Three Hours Alone",
            emoji = "🌿",
            message = "Disconnect to reconnect with yourself.",
            category = "Solitude & Self-Mastery",
            accentColorHex = "#F5B942"
        ),
        "night_journaling" to HabitNotificationIdentity(
            habitId = "night_journaling",
            title = "Night Journaling",
            emoji = "📖",
            message = "Before the day ends, capture what matters.",
            category = "Reflection & Growth",
            accentColorHex = "#FFC078"
        )
    )

    fun getIdentity(habitId: String, fallbackTitle: String = "Habit Nudge"): HabitNotificationIdentity {
        return identities[habitId] ?: HabitNotificationIdentity(
            habitId = habitId,
            title = fallbackTitle,
            emoji = "✨",
            message = "It's time for your daily habit nudge. Small steps lead to great habits.",
            category = "Daily Momentum",
            accentColorHex = "#FF8A3D"
        )
    }

    fun getAllIdentities(): List<HabitNotificationIdentity> = identities.values.toList()
}
