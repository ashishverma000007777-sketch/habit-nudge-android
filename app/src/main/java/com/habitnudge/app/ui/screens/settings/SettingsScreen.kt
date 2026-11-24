package com.habitnudge.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.backup.BackupRestoreManager
import com.habitnudge.app.data.preferences.AppTheme
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.reminder.HabitNotificationRegistry
import com.habitnudge.app.reminder.NotificationHelper
import com.habitnudge.app.reminder.alarm.HabitAlarmSoundService
import com.habitnudge.app.ui.components.FrostedGlassCard
import com.habitnudge.app.ui.components.GlassBackdrop
import com.habitnudge.app.ui.components.GlassDialog
import com.habitnudge.app.ui.components.GlassNavigationBar
import com.habitnudge.app.ui.components.GlassNotificationPreview
import com.habitnudge.app.ui.components.GlassTier
import com.habitnudge.app.ui.components.GlassToggle
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.ErrorRed
import com.habitnudge.app.ui.theme.LocalGlassPalette
import com.habitnudge.app.ui.theme.SuccessGreen
import com.habitnudge.app.ui.theme.WarningGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ToneItem(val title: String, val uriString: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    habitRepository: HabitRepository,
    alarmScheduler: AlarmScheduler,
    notificationHelper: NotificationHelper,
    backupRestoreManager: BackupRestoreManager,
    onNavigate: (String) -> Unit
) {
    val glass = LocalGlassPalette.current
    val context = LocalContext.current
    val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showResetDialog by remember { mutableStateOf(false) }
    var showQuietHoursStartPicker by remember { mutableStateOf(false) }
    var showQuietHoursEndPicker by remember { mutableStateOf(false) }
    var showBedtimePicker by remember { mutableStateOf(false) }
    var showNotificationToneDialog by remember { mutableStateOf(false) }
    var showAlarmToneDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var testNotificationFeedback by remember { mutableStateOf<String?>(null) }
    var backupFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestAlarmRinging by remember { mutableStateOf(false) }

    val identities = remember { HabitNotificationRegistry.getAllIdentities() }
    var selectedPreviewHabitId by remember { mutableStateOf(identities.firstOrNull()?.habitId ?: "daily_hydration") }
    val currentPreviewIdentity = remember(selectedPreviewHabitId) {
        HabitNotificationRegistry.getIdentity(selectedPreviewHabitId)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = backupRestoreManager.exportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    backupFeedback = true to "Backup exported successfully."
                } catch (e: Exception) {
                    backupFeedback = false to "Export failed: ${e.message ?: "Unable to write file"}"
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader().readText()
                    } ?: throw IllegalStateException("Empty backup file")
                    val summary = backupRestoreManager.importBackupJson(json)
                    backupFeedback = true to "Restored ${summary.habitsRestored} habits, ${summary.checkInsRestored} check-ins, ${summary.remindersRestored} reminders, and ${summary.journalsRestored} journals."
                } catch (e: Exception) {
                    backupFeedback = false to "Restore failed: Invalid or unreadable backup file."
                }
            }
        }
    }

    val userSettings = settings ?: return

    val areSystemNotificationsEnabled = remember(userSettings) {
        notificationHelper.areNotificationsEnabled()
    }

    GlassBackdrop {
        Scaffold(
            bottomBar = {
                GlassNavigationBar(
                    currentRoute = Screen.Settings.route,
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
                    Column {
                        Text(
                            text = "PREFERENCES & PRIVACY",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = glass.textSecondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 27.sp
                            ),
                            color = glass.textPrimary
                        )
                    }
                }

                // OS Level permission warning
                if (!areSystemNotificationsEnabled) {
                    item {
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            tier = GlassTier.High,
                            accentBorderColor = WarningGold.copy(alpha = 0.65f),
                            contentPadding = PaddingValues(14.dp),
                            onClick = { openSystemAppNotificationSettings(context) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Notifications Disabled in System",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = glass.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Tap here to grant Android notification permission so your habit nudges can arrive.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = glass.textSecondary
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = WarningGold)
                            }
                        }
                    }
                }

                // 1. Theme Group
                item {
                    SectionHeader(title = "Theme", subtitle = "Glass surface appearance")
                    Spacer(modifier = Modifier.height(6.dp))
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppTheme.values().forEach { themeOption ->
                                val isSelected = userSettings.theme == themeOption
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                                        .background(if (isSelected) AccentOrange.copy(alpha = 0.2f) else glass.surfaceLow)
                                        .border(
                                            Dimens.strokeThin,
                                            if (isSelected) AccentOrange else glass.borderSubtle,
                                            RoundedCornerShape(Dimens.radiusSmall)
                                        )
                                        .clickable {
                                            scope.launch { preferencesRepository.setTheme(themeOption) }
                                        }
                                        .padding(vertical = 11.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = themeOption.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) AccentOrange else glass.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Notification Settings & In-App Preview
                item {
                    SectionHeader(
                        title = "Notification Settings",
                        subtitle = "Heads-up alerts & branded in-app preview"
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SettingsToggleRow(
                                title = "Habit Reminders",
                                subtitle = "Master toggle for all scheduled nudges",
                                checked = userSettings.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        preferencesRepository.setNotificationsEnabled(enabled)
                                        alarmScheduler.rescheduleAllAlarms()
                                    }
                                }
                            )

                            Text(
                                text = "IN-APP REMINDER PREVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                color = AccentOrange
                            )

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
                                            .background(if (isSelected) accentCol.copy(alpha = 0.2f) else glass.surfaceLow)
                                            .border(
                                                Dimens.strokeThin,
                                                if (isSelected) accentCol else glass.borderSubtle,
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
                                                color = if (isSelected) accentCol else glass.textPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            GlassNotificationPreview(
                                identity = currentPreviewIdentity,
                                repeatingAlarmEnabled = userSettings.repeatingAlarmEnabled
                            )

                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = notificationHelper.showTestNotification(selectedPreviewHabitId)
                                        testNotificationFeedback = if (success) {
                                            "Test notification sent to your Android shade."
                                        } else {
                                            "Unable to post notification. Check Android permissions."
                                        }
                                        delay(4000)
                                        testNotificationFeedback = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = glass.surfaceHigh),
                                shape = RoundedCornerShape(Dimens.radiusPill),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(Dimens.strokeThin, AccentOrange.copy(alpha = 0.6f), RoundedCornerShape(Dimens.radiusPill))
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

                            SettingsActionRow(
                                icon = Icons.Default.Settings,
                                title = "Android Channel Settings",
                                subtitle = "Inspect OS notification channel overrides",
                                onClick = {
                                    val channelId = NotificationHelper.resolveChannelId(
                                        soundEnabled = userSettings.reminderSoundEnabled,
                                        vibrationEnabled = userSettings.reminderVibrationEnabled,
                                        soundToneUri = userSettings.soundToneUri
                                    )
                                    openSystemChannelSettings(context, channelId)
                                }
                            )
                        }
                    }
                }

                // 3. Alarm Sound Group
                item {
                    SectionHeader(
                        title = "Alarm Sound",
                        subtitle = "Repeating foreground alarm playback"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        accentBorderColor = if (userSettings.repeatingAlarmEnabled) AccentOrange.copy(alpha = 0.45f) else null,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SettingsToggleRow(
                                title = "Repeating Alarm Sound",
                                subtitle = "Loops alarm audio until responded to",
                                checked = userSettings.repeatingAlarmEnabled,
                                onCheckedChange = { scope.launch { preferencesRepository.setRepeatingAlarmEnabled(it) } }
                            )

                            if (userSettings.repeatingAlarmEnabled) {
                                SettingsActionRow(
                                    icon = Icons.Default.Alarm,
                                    title = "Alarm Tone",
                                    subtitle = userSettings.alarmToneTitle,
                                    accentSubtitle = true,
                                    onClick = { showAlarmToneDialog = true }
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                                        .background(glass.surfaceLow)
                                        .border(Dimens.strokeThin, glass.borderSubtle, RoundedCornerShape(Dimens.radiusSmall))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Alarm Volume", style = MaterialTheme.typography.titleSmall, color = glass.textPrimary)
                                        }
                                        Text(
                                            "${(userSettings.alarmVolume * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFeatureSettings = "tnum"
                                            ),
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
                                            inactiveTrackColor = glass.surfaceHigh
                                        )
                                    )
                                }

                                SettingsActionRow(
                                    icon = Icons.Default.Schedule,
                                    title = "Auto-Stop Ringing Timeout",
                                    subtitle = "${userSettings.alarmMaxDurationSeconds} seconds (Prevents endless ringing)",
                                    accentSubtitle = true,
                                    onClick = { showDurationDialog = true }
                                )

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
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
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

                // 4. Vibration & Standard Chime Group
                item {
                    SectionHeader(
                        title = "Vibration & Chime",
                        subtitle = "One-shot alert tone and haptic feedback"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SettingsToggleRow(
                                title = "Standard Reminder Sound",
                                subtitle = "One-shot chime when repeating alarm is off",
                                checked = userSettings.reminderSoundEnabled,
                                onCheckedChange = { scope.launch { preferencesRepository.setSoundEnabled(it) } }
                            )

                            if (userSettings.reminderSoundEnabled) {
                                SettingsActionRow(
                                    icon = Icons.Default.MusicNote,
                                    title = "Notification Tone",
                                    subtitle = userSettings.soundToneTitle,
                                    accentSubtitle = true,
                                    onClick = { showNotificationToneDialog = true }
                                )
                            }

                            SettingsToggleRow(
                                title = "Tactile Vibration",
                                subtitle = "Double-pulse haptic pattern for reminders & alarms",
                                checked = userSettings.reminderVibrationEnabled,
                                onCheckedChange = { scope.launch { preferencesRepository.setVibrationEnabled(it) } }
                            )
                        }
                    }
                }

                // 5. Reminder Schedule & 6. Quiet Hours
                item {
                    SectionHeader(
                        title = "Reminder Schedule & Quiet Hours",
                        subtitle = "Weekly cadence, bedtime anchor & silence window"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SettingsToggleRow(
                                title = "Week Starts on Monday",
                                subtitle = "Align weekly rhythm & analytics to Monday",
                                checked = userSettings.weekStartMonday,
                                onCheckedChange = { scope.launch { preferencesRepository.setWeekStartMonday(it) } }
                            )

                            SettingsActionRow(
                                icon = Icons.Default.Schedule,
                                title = "Bedtime Anchor",
                                subtitle = "%02d:%02d (Evening wind-down target)".format(
                                    userSettings.bedtimeHour, userSettings.bedtimeMinute
                                ),
                                accentSubtitle = true,
                                onClick = { showBedtimePicker = true }
                            )

                            SettingsToggleRow(
                                title = "Quiet Hours",
                                subtitle = "Silence nudges during rest hours",
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
                                }
                            )

                            if (userSettings.quietHoursEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FrostedGlassCard(
                                        modifier = Modifier.weight(1f),
                                        tier = GlassTier.Low,
                                        shape = RoundedCornerShape(Dimens.radiusSmall),
                                        contentPadding = PaddingValues(12.dp),
                                        onClick = { showQuietHoursStartPicker = true }
                                    ) {
                                        Column {
                                            Text("START", style = MaterialTheme.typography.labelSmall, color = glass.textSecondary)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "%02d:%02d".format(userSettings.quietHoursStartHour, userSettings.quietHoursStartMinute),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFeatureSettings = "tnum"
                                                ),
                                                color = AccentOrange
                                            )
                                        }
                                    }

                                    FrostedGlassCard(
                                        modifier = Modifier.weight(1f),
                                        tier = GlassTier.Low,
                                        shape = RoundedCornerShape(Dimens.radiusSmall),
                                        contentPadding = PaddingValues(12.dp),
                                        onClick = { showQuietHoursEndPicker = true }
                                    ) {
                                        Column {
                                            Text("END", style = MaterialTheme.typography.labelSmall, color = glass.textSecondary)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "%02d:%02d".format(userSettings.quietHoursEndHour, userSettings.quietHoursEndMinute),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFeatureSettings = "tnum"
                                                ),
                                                color = AccentOrange
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Backup and Restore
                item {
                    SectionHeader(
                        title = "Backup & Restore",
                        subtitle = "Offline JSON archive for habits, streaks & journals"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val fileName = "habit-nudge-backup-${LocalDate.now()}.json"
                                        exportLauncher.launch(fileName)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = glass.surfaceHigh),
                                    shape = RoundedCornerShape(Dimens.radiusPill)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export", color = glass.textPrimary, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = glass.surfaceHigh),
                                    shape = RoundedCornerShape(Dimens.radiusPill)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore", color = glass.textPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            AnimatedVisibility(
                                visible = backupFeedback != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                val isSuccess = backupFeedback?.first == true
                                Text(
                                    text = backupFeedback?.second ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSuccess) SuccessGreen else ErrorRed
                                )
                            }
                        }
                    }
                }

                // 8. Data Privacy & Reset
                item {
                    SectionHeader(title = "Data Privacy", subtitle = "Challenge resets & local-only guarantee")
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            tier = GlassTier.Medium,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Reset Individual Challenge",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = "Choose an active challenge to clear its check-in history and start fresh.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = glass.textSecondary
                                )
                                Button(
                                    onClick = { showResetDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = glass.surfaceLow),
                                    shape = RoundedCornerShape(Dimens.radiusPill),
                                    modifier = Modifier.border(Dimens.strokeThin, glass.borderSubtle, RoundedCornerShape(Dimens.radiusPill))
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reset a Challenge", color = AccentOrange)
                                }
                            }
                        }

                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            tier = GlassTier.Low,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Habit Nudge v1.0.0",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = glass.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "100% Offline & Private. No accounts, telemetry, or external servers. Your habits and reflections stay strictly on your device.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = glass.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        GlassDialog(
            onDismissRequest = { showResetDialog = false },
            title = "Select Challenge to Reset",
            actions = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = glass.textSecondary)
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier.height(220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits) { h ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusSmall))
                            .background(glass.surfaceLow)
                            .border(Dimens.strokeThin, glass.borderSubtle, RoundedCornerShape(Dimens.radiusSmall))
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
                        Text(h.title, style = MaterialTheme.typography.bodyLarge, color = glass.textPrimary)
                    }
                }
            }
        }
    }

    if (showQuietHoursStartPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = userSettings.quietHoursStartHour,
            initialMinute = userSettings.quietHoursStartMinute
        )
        GlassDialog(
            onDismissRequest = { showQuietHoursStartPicker = false },
            title = "Quiet Hours Start",
            actions = {
                TextButton(onClick = { showQuietHoursStartPicker = false }) {
                    Text("Cancel", color = glass.textSecondary)
                }
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
                            showQuietHoursStartPicker = false
                        }
                    }
                ) {
                    Text("Save", color = AccentOrange, fontWeight = FontWeight.Bold)
                }
            }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = pickerState)
            }
        }
    }

    if (showQuietHoursEndPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = userSettings.quietHoursEndHour,
            initialMinute = userSettings.quietHoursEndMinute
        )
        GlassDialog(
            onDismissRequest = { showQuietHoursEndPicker = false },
            title = "Quiet Hours End",
            actions = {
                TextButton(onClick = { showQuietHoursEndPicker = false }) {
                    Text("Cancel", color = glass.textSecondary)
                }
                TextButton(
                    onClick = {
                        scope.launch {
                            preferencesRepository.setQuietHours(
                                userSettings.quietHoursEnabled,
                                userSettings.quietHoursStartHour,
                                userSettings.quietHoursStartMinute,
                                pickerState.hour,
                                pickerState.minute
                            )
                            showQuietHoursEndPicker = false
                        }
                    }
                ) {
                    Text("Save", color = AccentOrange, fontWeight = FontWeight.Bold)
                }
            }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = pickerState)
            }
        }
    }

    if (showBedtimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = userSettings.bedtimeHour,
            initialMinute = userSettings.bedtimeMinute
        )
        GlassDialog(
            onDismissRequest = { showBedtimePicker = false },
            title = "Bedtime Anchor",
            actions = {
                TextButton(onClick = { showBedtimePicker = false }) {
                    Text("Cancel", color = glass.textSecondary)
                }
                TextButton(
                    onClick = {
                        scope.launch {
                            preferencesRepository.setBedtime(pickerState.hour, pickerState.minute)
                            showBedtimePicker = false
                        }
                    }
                ) {
                    Text("Save", color = AccentOrange, fontWeight = FontWeight.Bold)
                }
            }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = pickerState)
            }
        }
    }

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

    if (showDurationDialog) {
        val durations = listOf(30, 60, 90, 120, 300)
        GlassDialog(
            onDismissRequest = { showDurationDialog = false },
            title = "Auto-Stop Ringing Timeout",
            actions = {
                TextButton(onClick = { showDurationDialog = false }) {
                    Text("Cancel", color = glass.textSecondary)
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            .padding(10.dp),
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
                            color = if (isSelected) AccentOrange else glass.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val glass = LocalGlassPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = glass.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = glass.textSecondary)
        }
        GlassToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentSubtitle: Boolean = false,
    onClick: () -> Unit
) {
    val glass = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusSmall))
            .background(glass.surfaceLow)
            .border(Dimens.strokeThin, glass.borderSubtle, RoundedCornerShape(Dimens.radiusSmall))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = glass.textPrimary)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (accentSubtitle) AccentOrange else glass.textSecondary
                )
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = glass.textSecondary)
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
    val glass = LocalGlassPalette.current
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

    GlassDialog(
        onDismissRequest = {
            currentlyPlayingRingtone?.stop()
            onDismiss()
        },
        title = if (ringtoneType == RingtoneManager.TYPE_ALARM) "Select Alarm Tone" else "Select Notification Tone",
        actions = {
            TextButton(
                onClick = {
                    currentlyPlayingRingtone?.stop()
                    onDismiss()
                }
            ) {
                Text("Cancel", color = glass.textSecondary)
            }
            TextButton(
                onClick = {
                    currentlyPlayingRingtone?.stop()
                    onSelectTone(selectedUri, selectedTitle)
                }
            ) {
                Text("Select", color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        }
    ) {
        LazyColumn(modifier = Modifier.height(270.dp)) {
            items(tones) { item ->
                val isChecked = (selectedUri.isEmpty() && item.uriString.isEmpty()) || (selectedUri == item.uriString)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                        .background(if (isChecked) AccentOrange.copy(alpha = 0.16f) else Color.Transparent)
                        .clickable {
                            selectedUri = item.uriString
                            selectedTitle = item.title
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
                        color = if (isChecked) AccentOrange else glass.textPrimary
                    )
                }
            }
        }
    }
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
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun openSystemChannelSettings(context: Context, channelId: String) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
