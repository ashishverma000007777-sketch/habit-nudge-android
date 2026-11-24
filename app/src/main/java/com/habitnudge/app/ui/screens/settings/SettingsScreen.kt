package com.habitnudge.app.ui.screens.settings

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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.habitnudge.app.data.preferences.AppTheme
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.ui.components.HabitNudgeBottomBar
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.WarningGold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    habitRepository: HabitRepository,
    alarmScheduler: AlarmScheduler,
    onNavigate: (String) -> Unit
) {
    val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showResetDialog by remember { mutableStateOf(false) }
    var selectedResetHabitId by remember { mutableStateOf<String?>(null) }
    var showQuietHoursPicker by remember { mutableStateOf(false) }

    val userSettings = settings ?: return

    Scaffold(
        bottomBar = {
            HabitNudgeBottomBar(
                currentRoute = Screen.Settings.route,
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

                Column {
                    Text(
                        text = "PREFERENCES & PRIVACY",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Theme Setting
            item {
                SectionHeader(title = "Appearance", subtitle = "App color styling")
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.values().forEach { themeOption ->
                        val isSelected = userSettings.theme == themeOption
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Dimens.radiusSmall))
                                .background(if (isSelected) AccentOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentOrange else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(Dimens.radiusSmall)
                                )
                                .clickable {
                                    scope.launch { preferencesRepository.setTheme(themeOption) }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = themeOption.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // Notification Preferences
            item {
                SectionHeader(title = "Notifications & Quiet Hours", subtitle = "Nudge delivery parameters")
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Master notifications toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Habit Reminders", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("Receive scheduled nudges", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userSettings.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    preferencesRepository.setNotificationsEnabled(enabled)
                                    alarmScheduler.rescheduleAllAlarms()
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = AccentOrange)
                        )
                    }

                    // Quiet hours toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Quiet Hours", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                "%02d:%02d – %02d:%02d (Tap to change)".format(
                                    userSettings.quietHoursStartHour, userSettings.quietHoursStartMinute,
                                    userSettings.quietHoursEndHour, userSettings.quietHoursEndMinute
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentOrange,
                                modifier = Modifier.clickable { showQuietHoursPicker = true }
                            )
                        }
                        Switch(
                            checked = userSettings.quietHoursEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    preferencesRepository.setQuietHours(
                                        enabled,
                                        userSettings.quietHoursStartHour,
                                        userSettings.quietHoursStartMinute,
                                        userSettings.quietHoursEndHour,
                                        userSettings.quietHoursEndMinute
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = AccentOrange)
                        )
                    }

                    // Sound
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sound", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("Play chime for habit alerts", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userSettings.reminderSoundEnabled,
                            onCheckedChange = { scope.launch { preferencesRepository.setSoundEnabled(it) } },
                            colors = SwitchDefaults.colors(checkedTrackColor = AccentOrange)
                        )
                    }
                }
            }

            // Reset Individual Challenges
            item {
                SectionHeader(title = "Data Management", subtitle = "Challenge history controls")
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Reset Individual Challenge",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Choose an active challenge to reset its check-in records and start fresh.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(Dimens.radiusPill),
                        modifier = Modifier.border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusPill))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset a Challenge", color = AccentOrange)
                    }
                }
            }

            // About & Privacy Note
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Habit Nudge v1.0.0",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "100% Offline & Private. No accounts, telemetry, or external servers. Your habits and reflections stay strictly on your device.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Select Challenge to Reset") },
            text = {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(habits) { h ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Dimens.radiusSmall))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    scope.launch {
                                        habitRepository.restartHabit(h.id)
                                        alarmScheduler.rescheduleAllAlarms()
                                        showResetDialog = false
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(h.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showQuietHoursPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = userSettings.quietHoursStartHour,
            initialMinute = userSettings.quietHoursStartMinute
        )
        AlertDialog(
            onDismissRequest = { showQuietHoursPicker = false },
            title = { Text("Quiet Hours Start Time") },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = pickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            preferencesRepository.setQuietHours(
                                userSettings.quietHoursEnabled,
                                pickerState.hour,
                                pickerState.minute,
                                userSettings.quietHoursEndHour,
                                userSettings.quietHoursEndMinute
                            )
                            showQuietHoursPicker = false
                        }
                    }
                ) {
                    Text("Save", color = AccentOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuietHoursPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
