package com.habitnudge.app.ui.screens.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.local.entity.ReminderEntity
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.data.repository.ReminderRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.ui.components.EmptyStateCard
import com.habitnudge.app.ui.components.HabitNudgeBottomBar
import com.habitnudge.app.ui.components.ReminderCard
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.WarningGold
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCenterScreen(
    reminderRepository: ReminderRepository,
    habitRepository: HabitRepository,
    preferencesRepository: UserPreferencesRepository,
    alarmScheduler: AlarmScheduler,
    onNavigate: (String) -> Unit
) {
    val reminders by reminderRepository.getAllReminders().collectAsState(initial = emptyList())
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    val userSettings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }

    val habitTitleMap = remember(habits) {
        habits.associate { it.id to it.title }
    }

    val enabledReminders = reminders.filter { it.isEnabled }
    val disabledReminders = reminders.filter { !it.isEnabled }

    Scaffold(
        bottomBar = {
            HabitNudgeBottomBar(
                currentRoute = Screen.Reminders.route,
                onNavigate = onNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SMART REMINDERS",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Nudge Center",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(Dimens.radiusPill)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            // Quiet hours card
            item {
                val settings = userSettings
                if (settings != null && settings.quietHoursEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                            .padding(16.dp)
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
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Alerts silenced between %02d:%02d and %02d:%02d".format(
                                        settings.quietHoursStartHour, settings.quietHoursStartMinute,
                                        settings.quietHoursEndHour, settings.quietHoursEndMinute
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Active / Enabled Reminders
            item {
                SectionHeader(
                    title = "Enabled Nudges (${enabledReminders.size})",
                    subtitle = "Scheduled precision alarms"
                )
            }

            if (enabledReminders.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Enabled Reminders",
                        description = "Enable reminders to get smart native notifications at your preferred times.",
                        icon = Icons.Default.NotificationsOff
                    )
                }
            } else {
                items(enabledReminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        habitTitle = habitTitleMap[reminder.habitId] ?: "Habit Nudge",
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

            // Disabled Reminders
            if (disabledReminders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Paused Reminders (${disabledReminders.size})",
                        subtitle = "Toggle on to resume scheduling"
                    )
                }

                items(disabledReminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        habitTitle = habitTitleMap[reminder.habitId] ?: "Habit Nudge",
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

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddDialog && habits.isNotEmpty()) {
        var selectedHabitId by remember { mutableStateOf(habits.first().id) }
        var reminderLabel by remember { mutableStateOf("Daily Nudge") }
        val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 30)

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Scheduled Nudge") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select Habit Challenge:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Simple habit selection buttons/chips
                    LazyColumn(modifier = Modifier.height(110.dp)) {
                        items(habits) { h ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Dimens.radiusSmall))
                                    .clickable { selectedHabitId = h.id }
                                    .background(if (selectedHabitId == h.id) AccentOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        1.dp,
                                        if (selectedHabitId == h.id) AccentOrange else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(Dimens.radiusSmall)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = h.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedHabitId == h.id) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        TimePicker(state = timePickerState)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val newId = reminderRepository.addReminder(
                                habitId = selectedHabitId,
                                timeHour = timePickerState.hour,
                                timeMinute = timePickerState.minute,
                                label = reminderLabel
                            )
                            val created = reminderRepository.getReminderById(newId)
                            if (created != null) {
                                alarmScheduler.scheduleReminder(created)
                            }
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Schedule", color = AccentOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
