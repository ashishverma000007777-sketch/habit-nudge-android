package com.habitnudge.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.habitnudge.app.data.local.entity.ReminderEntity

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    habitTitle: String,
    soundStatusLabel: String = "Chime",
    vibrationEnabled: Boolean = true,
    isRepeatingAlarm: Boolean = false,
    onToggle: (Boolean) -> Unit,
    onSnooze: ((Int) -> Unit)? = null,
    onClearSnooze: (() -> Unit)? = null,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassReminderCard(
        reminder = reminder,
        habitTitle = habitTitle,
        soundStatusLabel = soundStatusLabel,
        vibrationEnabled = vibrationEnabled,
        isRepeatingAlarm = isRepeatingAlarm,
        onToggle = onToggle,
        onSnooze = onSnooze,
        onClearSnooze = onClearSnooze,
        onDelete = onDelete,
        modifier = modifier
    )
}
