package com.habitnudge.app.ui.screens.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.data.repository.ReminderRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.ui.components.EmptyStateCard
import com.habitnudge.app.ui.components.FrostedGlassCard
import com.habitnudge.app.ui.components.GlassBackdrop
import com.habitnudge.app.ui.components.GlassBottomSheet
import com.habitnudge.app.ui.components.GlassNavigationBar
import com.habitnudge.app.ui.components.GlassReminderCard
import com.habitnudge.app.ui.components.GlassTier
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette
import com.habitnudge.app.ui.theme.WarningGold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCenterScreen(
    reminderRepository: ReminderRepository,
    habitRepository: HabitRepository,
    preferencesRepository: UserPreferencesRepository,
    alarmScheduler: AlarmScheduler,
    onNavigate: (String) -> Unit
) {
    val glass = LocalGlassPalette.current
    val reminders by reminderRepository.getAllReminders().collectAsState(initial = emptyList())
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    val userSettings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }

    val habitTitleMap = remember(habits) {
        habits.associate { it.id to it.title }
    }

    val enabledReminders = reminders.filter { it.isEnabled }
    val disabledReminders = reminders.filter { !it.isEnabled }

    val isRepeatingAlarm = userSettings?.repeatingAlarmEnabled ?: false
    val vibrationEnabled = userSettings?.reminderVibrationEnabled ?: true
    val soundLabel = when {
        isRepeatingAlarm -> "Alarm"
        userSettings?.reminderSoundEnabled != false -> "Chime"
        else -> "Silent"
    }

    GlassBackdrop {
        Scaffold(
            bottomBar = {
                GlassNavigationBar(
                    currentRoute = Screen.Reminders.route,
                    onNavigate = onNavigate
                )
            },
            containerColor = Color.Transparent
        ) { _ ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.spaceScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = Dimens.bottomBarClearance)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SMART REMINDERS",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                                color = glass.textSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Nudge Center",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 27.sp
                                ),
                                color = glass.textPrimary
                            )
                        }

                        Button(
                            onClick = { showAddSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(Dimens.radiusPill),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Delivery & Quiet Hours Summary
                item {
                    val settings = userSettings
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (settings != null) {
                            FrostedGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                tier = GlassTier.Low,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                onClick = { onNavigate(Screen.Settings.route) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (settings.repeatingAlarmEnabled) Icons.Default.Alarm else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = null,
                                            tint = AccentOrange,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = if (settings.repeatingAlarmEnabled) "Repeating Alarm (${(settings.alarmVolume * 100).toInt()}%)"
                                            else if (settings.reminderSoundEnabled) "Standard Sound"
                                            else "Silent Alerts",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = glass.textPrimary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = null,
                                            tint = if (settings.reminderVibrationEnabled) AccentOrange else glass.textTertiary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (settings.reminderVibrationEnabled) "Vibrate On" else "Vibrate Off",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = glass.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        if (settings != null && settings.quietHoursEnabled) {
                            FrostedGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                tier = GlassTier.Medium,
                                accentBorderColor = WarningGold.copy(alpha = 0.4f),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = null,
                                        tint = WarningGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Quiet Hours Active",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = glass.textPrimary
                                        )
                                        Text(
                                            text = "Alerts silenced between %02d:%02d and %02d:%02d".format(
                                                settings.quietHoursStartHour, settings.quietHoursStartMinute,
                                                settings.quietHoursEndHour, settings.quietHoursEndMinute
                                            ),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                            color = glass.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active / Enabled Reminders
                item {
                    SectionHeader(
                        title = "Active Nudges (${enabledReminders.size})",
                        subtitle = "Precision alarms with quick snooze"
                    )
                }

                if (enabledReminders.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Active Reminders",
                            description = "Enable reminders or add a new schedule to get gentle nudges at your preferred times.",
                            icon = Icons.Default.NotificationsOff,
                            actionLabel = "Add Reminder",
                            onActionClick = { showAddSheet = true }
                        )
                    }
                } else {
                    items(enabledReminders, key = { it.id }) { reminder ->
                        GlassReminderCard(
                            reminder = reminder,
                            habitTitle = habitTitleMap[reminder.habitId] ?: "Habit Nudge",
                            soundStatusLabel = soundLabel,
                            vibrationEnabled = vibrationEnabled,
                            isRepeatingAlarm = isRepeatingAlarm,
                            onToggle = { isChecked ->
                                scope.launch {
                                    reminderRepository.toggleReminder(reminder.id, isChecked)
                                    if (isChecked) {
                                        alarmScheduler.scheduleReminder(reminder.copy(isEnabled = true))
                                    } else {
                                        alarmScheduler.cancelReminder(reminder.id)
                                    }
                                }
                            },
                            onSnooze = { mins ->
                                scope.launch {
                                    reminderRepository.snoozeReminder(reminder.id, mins)
                                    reminderRepository.getReminderById(reminder.id)?.let { updated ->
                                        alarmScheduler.scheduleReminder(updated)
                                    }
                                }
                            },
                            onClearSnooze = {
                                scope.launch {
                                    reminderRepository.clearSnooze(reminder.id)
                                    reminderRepository.getReminderById(reminder.id)?.let { updated ->
                                        alarmScheduler.scheduleReminder(updated)
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    alarmScheduler.cancelReminder(reminder.id)
                                    reminderRepository.deleteReminder(reminder.id)
                                }
                            }
                        )
                    }
                }

                // Inactive / Disabled Reminders
                if (disabledReminders.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader(
                            title = "Inactive Nudges (${disabledReminders.size})",
                            subtitle = "Toggle on to resume scheduling"
                        )
                    }

                    items(disabledReminders, key = { it.id }) { reminder ->
                        GlassReminderCard(
                            reminder = reminder,
                            habitTitle = habitTitleMap[reminder.habitId] ?: "Habit Nudge",
                            soundStatusLabel = soundLabel,
                            vibrationEnabled = vibrationEnabled,
                            isRepeatingAlarm = isRepeatingAlarm,
                            onToggle = { isChecked ->
                                scope.launch {
                                    reminderRepository.toggleReminder(reminder.id, isChecked)
                                    if (isChecked) {
                                        alarmScheduler.scheduleReminder(reminder.copy(isEnabled = true))
                                    } else {
                                        alarmScheduler.cancelReminder(reminder.id)
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    alarmScheduler.cancelReminder(reminder.id)
                                    reminderRepository.deleteReminder(reminder.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet && habits.isNotEmpty()) {
        var selectedHabitId by remember { mutableStateOf(habits.first().id) }
        val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 30)
        val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5, 6, 7) }
        val dayLabels = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")

        GlassBottomSheet(onDismissRequest = { showAddSheet = false }) {
            Text(
                text = "New Scheduled Nudge",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HABIT CHALLENGE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = glass.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.height(112.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(habits) { h ->
                    val isSelected = selectedHabitId == h.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusSmall))
                            .background(if (isSelected) AccentOrange.copy(alpha = 0.18f) else glass.surfaceLow)
                            .border(
                                Dimens.strokeThin,
                                if (isSelected) AccentOrange else glass.borderSubtle,
                                RoundedCornerShape(Dimens.radiusSmall)
                            )
                            .clickable { selectedHabitId = h.id }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = h.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) AccentOrange else glass.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "REPEAT SCHEDULE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = glass.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEach { (dayNum, letter) ->
                    val active = selectedDays.contains(dayNum)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (active) AccentOrange.copy(alpha = 0.22f) else glass.surfaceLow)
                            .border(
                                Dimens.strokeThin,
                                if (active) AccentOrange else glass.borderSubtle,
                                CircleShape
                            )
                            .clickable {
                                if (active && selectedDays.size > 1) selectedDays.remove(dayNum)
                                else if (!active) selectedDays.add(dayNum)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (active) AccentOrange else glass.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = timePickerState)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        val newId = reminderRepository.addReminder(
                            habitId = selectedHabitId,
                            timeHour = timePickerState.hour,
                            timeMinute = timePickerState.minute,
                            repeatDays = selectedDays.sorted(),
                            label = "Daily Nudge"
                        )
                        val created = reminderRepository.getReminderById(newId)
                        if (created != null) {
                            alarmScheduler.scheduleReminder(created)
                        }
                        showAddSheet = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(Dimens.radiusPill)
            ) {
                Text("Schedule Nudge", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
