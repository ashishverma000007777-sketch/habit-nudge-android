package com.habitnudge.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.preferences.AppTheme
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.preferences.UserSettings
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.reminder.HabitNotificationIdentity
import com.habitnudge.app.reminder.HabitNotificationRegistry
import com.habitnudge.app.reminder.NotificationHelper
import com.habitnudge.app.reminder.alarm.HabitAlarmSoundService
import com.habitnudge.app.ui.components.HabitNudgeBottomBar
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.WarningGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ToneItem(val title: String, val uriString: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    habitRepository: HabitRepository,
    alarmScheduler: AlarmScheduler,
    notificationHelper: NotificationHelper,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showResetDialog by remember { mutableStateOf(false) }
    var showQuietHoursPicker by remember { mutableStateOf(false) }
    var showNotificationToneDialog by remember { mutableStateOf(false) }
    var showAlarmToneDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var testNotificationFeedback by remember { mutableStateOf<String?>(null) }
    var isTestAlarmRinging by remember { mutableStateOf(false) }

    val identities = remember { HabitNotificationRegistry.getAllIdentities() }
    var selectedPreviewHabitId by remember { mutableStateOf(identities.firstOrNull()?.habitId ?: "daily_hydration") }
    val currentPreviewIdentity = remember(selectedPreviewHabitId) {
        HabitNotificationRegistry.getIdentity(selectedPreviewHabitId)
    }

    val userSettings = settings ?: return

    val areSystemNotificationsEnabled = remember(userSettings) {
        notificationHelper.areNotificationsEnabled()
    }

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

            // OS Level permission warning
            if (!areSystemNotificationsEnabled) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                            .background(WarningGold.copy(alpha = 0.15f))
                            .border(1.dp, WarningGold, RoundedCornerShape(Dimens.radiusMedium))
                            .clickable { openSystemAppNotificationSettings(context) }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Notifications Disabled in System",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Tap here to grant Android notification permission so your habit nudges can arrive.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = WarningGold)
                        }
                    }
                }
            }

            // Appearance
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

            // Repeating Alarm Sound Engine (Highlighted Section)
            item {
                SectionHeader(
                    title = "Repeating Alarm Sound Engine",
                    subtitle = "Attention-grabbing looping alarm playback"
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, AccentOrange.copy(alpha = 0.6f), RoundedCornerShape(Dimens.radiusMedium))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Master Repeating Alarm toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Repeating Alarm Sound",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                "Loops audio continuously until responded to",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userSettings.repeatingAlarmEnabled,
                            onCheckedChange = { scope.launch { preferencesRepository.setRepeatingAlarmEnabled(it) } },
                            colors = SwitchDefaults.colors(checkedTrackColor = AccentOrange)
                        )
                    }

                    if (userSettings.repeatingAlarmEnabled) {
                        // Alarm Tone Selection Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Dimens.radiusSmall))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusSmall))
                                .clickable { showAlarmToneDialog = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Alarm Tone",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        userSettings.alarmToneTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentOrange
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Alarm Volume Slider
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Dimens.radiusSmall))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusSmall))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Alarm Volume", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                                }
                                Text(
                                    "${(userSettings.alarmVolume * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AccentOrange
                                )
                            }
                            Slider(
                                value = userSettings.alarmVolume,
                                onValueChange = { scope.launch { preferencesRepository.setAlarmVolume(it) } },
                                valueRange = 0.05f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentOrange,
                                    activeTrackColor = AccentOrange,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        // Maximum Ringing Duration Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Dimens.radiusSmall))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusSmall))
                                .clickable { showDurationDialog = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Stop Ringing Timeout", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                                Text(
                                    "${userSettings.alarmMaxDurationSeconds} seconds (Prevents infinite ringing)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentOrange
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Test Repeating Alarm Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isTestAlarmRinging) {
                                Button(
                                    onClick = {
                                        isTestAlarmRinging = true
                                        HabitAlarmSoundService.startAlarm(
                                            context = context,
                                            habitId = selectedPreviewHabitId,
                                            reminderId = -1L,
                                            maxDurationSeconds = userSettings.alarmMaxDurationSeconds,
                                            volume = userSettings.alarmVolume,
                                            toneUri = userSettings.alarmToneUri,
                                            vibrationEnabled = userSettings.reminderVibrationEnabled,
                                            isTest = true
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                    shape = RoundedCornerShape(Dimens.radiusPill),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Test Repeating Alarm Sound", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        isTestAlarmRinging = false
                                        HabitAlarmSoundService.stopAlarm(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    shape = RoundedCornerShape(Dimens.radiusPill),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Stop Alarm Now", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Notification Experience & Preview
            item {
                SectionHeader(
                    title = "Notification Experience",
                    subtitle = "Premium visual identity and live preview"
                )
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
                    Text(
                        text = "PREVIEW A HABIT NUDGE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = AccentOrange
                    )

                    // Habit Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        identities.forEach { identity ->
                            val isSelected = identity.habitId == selectedPreviewHabitId
                            val accentCol = try {
                                Color(android.graphics.Color.parseColor(identity.accentColorHex))
                            } catch (e: Exception) {
                                AccentOrange
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.radiusPill))
                                    .background(if (isSelected) accentCol.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        if (isSelected) accentCol else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(Dimens.radiusPill)
                                    )
                                    .clickable { selectedPreviewHabitId = identity.habitId }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = identity.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = identity.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) accentCol else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }

                    // Notification Card Mockup
                    NotificationPreviewCard(identity = currentPreviewIdentity)

                    // Test Notification Button
                    Button(
                        onClick = {
                            scope.launch {
                                val success = notificationHelper.showTestNotification(selectedPreviewHabitId)
                                if (success) {
                                    testNotificationFeedback = "Test notification sent! Check your notification shade."
                                } else {
                                    testNotificationFeedback = "Unable to post notification. Check Android permissions."
                                }
                                delay(4000)
                                testNotificationFeedback = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(Dimens.radiusPill),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AccentOrange, RoundedCornerShape(Dimens.radiusPill))
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Standard Notification Test", color = AccentOrange, fontWeight = FontWeight.SemiBold)
                    }

                    AnimatedVisibility(
                        visible = testNotificationFeedback != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = testNotificationFeedback ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = AccentOrange
                        )
                    }
                }
            }

            // General Delivery & Quiet Hours
            item {
                SectionHeader(
                    title = "General Delivery & Haptics",
                    subtitle = "One-shot sound, vibration and quiet hours"
                )
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

                    // Notification Tone (One-shot)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Standard Reminder Sound", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("One-shot chime when repeating alarm is off", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userSettings.reminderSoundEnabled,
                            onCheckedChange = { scope.launch { preferencesRepository.setSoundEnabled(it) } },
                            colors = SwitchDefaults.colors(checkedTrackColor = AccentOrange)
                        )
                    }

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tactile Vibration", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text("Haptic pulses during reminders and alarms", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userSettings.reminderVibrationEnabled,
                            onCheckedChange = { scope.launch { preferencesRepository.setVibrationEnabled(it) } },
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

                    // Android System Channel Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusSmall))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusSmall))
                            .clickable {
                                val channelId = NotificationHelper.resolveChannelId(
                                    soundEnabled = userSettings.reminderSoundEnabled,
                                    vibrationEnabled = userSettings.reminderVibrationEnabled,
                                    soundToneUri = userSettings.soundToneUri
                                )
                                openSystemChannelSettings(context, channelId)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Android Channel Settings", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                                Text("Inspect system notification channel & overrides", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

    // Reset Challenge Dialog
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

    // Quiet Hours Dialog
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

    // Alarm Tone Picker Dialog
    if (showAlarmToneDialog) {
        TonePickerDialog(
            context = context,
            ringtoneType = RingtoneManager.TYPE_ALARM,
            defaultTitle = "Default alarm sound",
            currentUri = userSettings.alarmToneUri,
            currentTitle = userSettings.alarmToneTitle,
            onDismiss = { showAlarmToneDialog = false },
            onSelectTone = { uri, title ->
                scope.launch {
                    preferencesRepository.setAlarmTone(uri, title)
                    showAlarmToneDialog = false
                }
            }
        )
    }

    // Standard Notification Tone Picker Dialog
    if (showNotificationToneDialog) {
        TonePickerDialog(
            context = context,
            ringtoneType = RingtoneManager.TYPE_NOTIFICATION,
            defaultTitle = "Default notification tone",
            currentUri = userSettings.soundToneUri,
            currentTitle = userSettings.soundToneTitle,
            onDismiss = { showNotificationToneDialog = false },
            onSelectTone = { uri, title ->
                scope.launch {
                    preferencesRepository.setSoundTone(uri, title)
                    showNotificationToneDialog = false
                }
            }
        )
    }

    // Max Ringing Duration Dialog
    if (showDurationDialog) {
        val durations = listOf(30, 60, 90, 120, 300)
        AlertDialog(
            onDismissRequest = { showDurationDialog = false },
            title = { Text("Auto-Stop Ringing Timeout") },
            text = {
                Column {
                    durations.forEach { seconds ->
                        val isSelected = userSettings.alarmMaxDurationSeconds == seconds
                        val label = when (seconds) {
                            60 -> "60 seconds (Recommended)"
                            120 -> "2 minutes"
                            300 -> "5 minutes"
                            else -> "$seconds seconds"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Dimens.radiusSmall))
                                .background(if (isSelected) AccentOrange.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    scope.launch {
                                        preferencesRepository.setAlarmMaxDurationSeconds(seconds)
                                        showDurationDialog = false
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = AccentOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDurationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotificationPreviewCard(identity: HabitNotificationIdentity) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(identity.accentColorHex))
    } catch (e: Exception) {
        AccentOrange
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(Color(0xFF1E2128))
            .border(1.dp, Color(0xFF2C3240), RoundedCornerShape(Dimens.radiusMedium))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AccentOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚡", fontSize = 10.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Habit Nudge",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB0B7C3)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• now",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6E788B))
                    )
                }

                Text(
                    text = identity.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF8A94A6)
                    )
                )
            }

            // Body: Large Icon + Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = identity.emoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${identity.emoji} ${identity.title}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = identity.message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFCBD2DF)
                        )
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.radiusPill))
                        .background(Color(0xFFE53935).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFE53935).copy(alpha = 0.6f), RoundedCornerShape(Dimens.radiusPill))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Stop Alarm",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.radiusPill))
                        .background(AccentOrange.copy(alpha = 0.25f))
                        .border(1.dp, AccentOrange.copy(alpha = 0.6f), RoundedCornerShape(Dimens.radiusPill))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Mark Done",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentOrange
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.radiusPill))
                        .background(Color(0xFF2C3240))
                        .border(1.dp, Color(0xFF3E4759), RoundedCornerShape(Dimens.radiusPill))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Snooze 10m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD2DF)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TonePickerDialog(
    context: Context,
    ringtoneType: Int,
    defaultTitle: String,
    currentUri: String,
    currentTitle: String,
    onDismiss: () -> Unit,
    onSelectTone: (uri: String, title: String) -> Unit
) {
    var currentlyPlayingRingtone by remember { mutableStateOf<Ringtone?>(null) }
    var selectedUri by remember { mutableStateOf(currentUri) }
    var selectedTitle by remember { mutableStateOf(currentTitle) }

    val tones = remember {
        val list = mutableListOf<ToneItem>()
        list.add(ToneItem(title = defaultTitle, uriString = ""))
        try {
            val ringtoneManager = RingtoneManager(context).apply {
                setType(ringtoneType)
            }
            val cursor = ringtoneManager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position).toString()
                list.add(ToneItem(title = title, uriString = uri))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    DisposableEffect(Unit) {
        onDispose {
            currentlyPlayingRingtone?.stop()
        }
    }

    AlertDialog(
        onDismissRequest = {
            currentlyPlayingRingtone?.stop()
            onDismiss()
        },
        title = { Text(if (ringtoneType == RingtoneManager.TYPE_ALARM) "Select Alarm Tone" else "Select Notification Tone") },
        text = {
            LazyColumn(modifier = Modifier.height(280.dp)) {
                items(tones) { item ->
                    val isChecked = (selectedUri.isEmpty() && item.uriString.isEmpty()) || (selectedUri == item.uriString)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusSmall))
                            .background(if (isChecked) AccentOrange.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                selectedUri = item.uriString
                                selectedTitle = item.title

                                // Play preview tone
                                currentlyPlayingRingtone?.stop()
                                try {
                                    val uriToPlay = if (item.uriString.isEmpty()) {
                                        RingtoneManager.getDefaultUri(ringtoneType)
                                    } else {
                                        Uri.parse(item.uriString)
                                    }
                                    val r = RingtoneManager.getRingtone(context, uriToPlay)
                                    r?.play()
                                    currentlyPlayingRingtone = r
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isChecked,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = AccentOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isChecked) AccentOrange else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    currentlyPlayingRingtone?.stop()
                    onSelectTone(selectedUri, selectedTitle)
                }
            ) {
                Text("Select", color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    currentlyPlayingRingtone?.stop()
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun openSystemAppNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openSystemChannelSettings(context: Context, channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openSystemAppNotificationSettings(context)
        }
    } else {
        openSystemAppNotificationSettings(context)
    }
}
