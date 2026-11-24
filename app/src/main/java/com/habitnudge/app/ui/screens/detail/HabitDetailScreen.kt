package com.habitnudge.app.ui.screens.detail

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.habitnudge.app.reminder.HabitNotificationRegistry
import com.habitnudge.app.ui.components.CompletionButton
import com.habitnudge.app.ui.components.EmptyStateCard
import com.habitnudge.app.ui.components.FrostedGlassCard
import com.habitnudge.app.ui.components.GlassBackdrop
import com.habitnudge.app.ui.components.GlassBottomSheet
import com.habitnudge.app.ui.components.GlassDialog
import com.habitnudge.app.ui.components.GlassIconButton
import com.habitnudge.app.ui.components.GlassProgressRing
import com.habitnudge.app.ui.components.GlassReminderCard
import com.habitnudge.app.ui.components.GlassTier
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette
import com.habitnudge.app.ui.theme.SuccessGreen
import com.habitnudge.app.ui.theme.WarningGold
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    habitRepository: HabitRepository,
    reminderRepository: ReminderRepository,
    preferencesRepository: UserPreferencesRepository,
    alarmScheduler: AlarmScheduler,
    onBack: () -> Unit
) {
    val glass = LocalGlassPalette.current
    val habitWithProgress by habitRepository.getHabitWithProgress(habitId).collectAsState(initial = null)
    val reminders by reminderRepository.getRemindersForHabit(habitId).collectAsState(initial = emptyList())
    val checkIns by habitRepository.getCheckInsForHabit(habitId).collectAsState(initial = emptyList())
    val userSettings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var showRestartDialog by remember { mutableStateOf(false) }
    var showAddReminderSheet by remember { mutableStateOf(false) }

    val item = habitWithProgress ?: return
    val habit = item.habit
    val progress = item.progress
    val identity = HabitNotificationRegistry.getIdentity(habit.id, habit.title)

    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.accentColorHex))
    } catch (e: Exception) {
        AccentOrange
    }

    val startDateFormatted = habit.startDate?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } ?: "Not started"

    val soundLabel = when {
        userSettings?.repeatingAlarmEnabled == true -> "Alarm"
        userSettings?.reminderSoundEnabled != false -> "Chime"
        else -> "Silent"
    }
    val vibrationEnabled = userSettings?.reminderVibrationEnabled ?: true
    val isRepeatingAlarm = userSettings?.repeatingAlarmEnabled ?: false

    GlassBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Floating Glass Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spaceScreenHorizontal, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    size = 42.dp
                )
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = glass.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.spaceScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
            ) {
                // Large Habit Identity Hero Card
                item {
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.High,
                        accentBorderColor = accentColor.copy(alpha = 0.35f),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(accentColor.copy(alpha = 0.16f))
                                            .border(Dimens.strokeThin, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = identity.emoji, fontSize = 22.sp)
                                    }
                                    Text(
                                        text = habit.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                        color = accentColor
                                    )
                                }

                                val (statusText, statusColor) = when {
                                    !habit.isActive -> "Inactive" to glass.textSecondary
                                    habit.isPaused -> "Paused" to WarningGold
                                    progress.isChallengeComplete -> "Completed" to SuccessGreen
                                    else -> "Active" to SuccessGreen
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Dimens.radiusPill))
                                        .background(statusColor.copy(alpha = 0.16f))
                                        .border(Dimens.strokeThin, statusColor.copy(alpha = 0.35f), RoundedCornerShape(Dimens.radiusPill))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = glass.textPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = habit.tagline,
                                style = MaterialTheme.typography.bodyLarge,
                                color = glass.textSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = glass.textPrimary.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Started: $startDateFormatted • Target: ${habit.durationDays} Days",
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                color = accentColor
                            )
                        }
                    }
                }

                // Glass Progress Panel + Streak Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FrostedGlassCard(
                            modifier = Modifier.weight(1f),
                            tier = GlassTier.Medium,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GlassProgressRing(
                                    progress = progress.progressFraction,
                                    size = 76.dp,
                                    strokeWidth = 7.dp,
                                    progressColor = accentColor
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "${progress.completedDaysCount} of ${habit.durationDays} Days",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = "${progress.remainingDays} days remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glass.textSecondary
                                )
                            }
                        }

                        FrostedGlassCard(
                            modifier = Modifier.weight(1f),
                            tier = GlassTier.Medium,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                StreakMetric(
                                    label = "CURRENT STREAK",
                                    value = "${progress.currentStreak} Days",
                                    valueColor = WarningGold
                                )
                                StreakMetric(
                                    label = "BEST STREAK",
                                    value = "${progress.bestStreak} Days",
                                    valueColor = glass.textPrimary
                                )
                                StreakMetric(
                                    label = "MISSED DAYS",
                                    value = "${progress.missedDaysCount} Days",
                                    valueColor = glass.textSecondary
                                )
                            }
                        }
                    }
                }

                // Today's Action Toggle
                item {
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        accentBorderColor = if (progress.isCompletedToday) SuccessGreen.copy(alpha = 0.55f) else AccentOrange.copy(alpha = 0.3f),
                        contentPadding = PaddingValues(16.dp),
                        onClick = {
                            scope.launch { habitRepository.toggleCheckInToday(habit.id) }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (progress.isCompletedToday) "Completed for Today" else "Mark Today's Nudge",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (progress.isCompletedToday) SuccessGreen else glass.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (progress.isCompletedToday) "Tap to uncheck if recorded by mistake" else "Tap checkmark to record today's completion",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = glass.textSecondary
                                )
                            }

                            CompletionButton(
                                isCompleted = progress.isCompletedToday,
                                onToggle = {
                                    scope.launch { habitRepository.toggleCheckInToday(habit.id) }
                                }
                            )
                        }
                    }
                }

                // Daily Checklist / Challenge Directives
                item {
                    SectionHeader(
                        title = "Daily Checklist",
                        subtitle = "Actionable directives for this challenge"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            habit.tasks.forEach { task ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(19.dp)
                                            .clip(CircleShape)
                                            .background(accentColor.copy(alpha = 0.16f))
                                            .border(1.dp, accentColor.copy(alpha = 0.7f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = task,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = glass.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 14-day Completion History
                item {
                    SectionHeader(
                        title = "14-Day Completion History",
                        subtitle = "Recent two-week execution"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val today = LocalDate.now()
                    val completedDates = checkIns.filter { it.completed }.map { it.date }.toSet()

                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tier = GlassTier.Medium,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (13 downTo 0).forEach { daysAgo ->
                                val date = today.minusDays(daysAgo.toLong())
                                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val isDone = completedDates.contains(dateStr)
                                val isToday = daysAgo == 0

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = date.dayOfWeek.name.take(1),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isToday) AccentOrange else glass.textSecondary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isDone -> SuccessGreen
                                                    isToday -> AccentOrange.copy(alpha = 0.25f)
                                                    else -> glass.surfaceLow
                                                }
                                            )
                                            .border(
                                                width = if (isToday) 1.25.dp else 0.75.dp,
                                                color = when {
                                                    isToday -> AccentOrange
                                                    isDone -> SuccessGreen.copy(alpha = 0.7f)
                                                    else -> glass.borderSubtle
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                // Reminder Configuration
                item {
                    SectionHeader(
                        title = "Reminder Configuration",
                        subtitle = "Scheduled nudges for this habit",
                        actionText = "+ Add Time",
                        onActionClick = { showAddReminderSheet = true }
                    )
                }

                if (reminders.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Reminders Configured",
                            description = "Add a daily or weekly time to get gentle nudges for ${habit.title}.",
                            icon = Icons.Default.NotificationsOff,
                            actionLabel = "Add Reminder",
                            onActionClick = { showAddReminderSheet = true }
                        )
                    }
                } else {
                    items(reminders, key = { it.id }) { reminder ->
                        GlassReminderCard(
                            reminder = reminder,
                            habitTitle = habit.title,
                            soundStatusLabel = soundLabel,
                            vibrationEnabled = vibrationEnabled,
                            isRepeatingAlarm = isRepeatingAlarm,
                            onToggle = { checked ->
                                scope.launch {
                                    reminderRepository.toggleReminder(reminder.id, checked)
                                    if (checked) {
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

                // Challenge Controls
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    SectionHeader(title = "Challenge Controls")
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    habitRepository.setHabitPaused(habit.id, !habit.isPaused)
                                    alarmScheduler.rescheduleAllAlarms()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Dimens.radiusPill),
                            border = androidx.compose.foundation.BorderStroke(Dimens.strokeThin, glass.borderSubtle)
                        ) {
                            Icon(
                                imageVector = if (habit.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = glass.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (habit.isPaused) "Resume" else "Pause", color = glass.textPrimary)
                        }

                        OutlinedButton(
                            onClick = { showRestartDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Dimens.radiusPill),
                            border = androidx.compose.foundation.BorderStroke(Dimens.strokeThin, glass.borderSubtle)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = glass.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restart", color = glass.textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                habitRepository.setHabitActive(habit.id, !habit.isActive)
                                alarmScheduler.rescheduleAllAlarms()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (habit.isActive) glass.surfaceHigh else AccentOrange
                        ),
                        shape = RoundedCornerShape(Dimens.radiusPill)
                    ) {
                        Text(
                            text = if (habit.isActive) "Deactivate Challenge" else "Activate Challenge",
                            color = if (habit.isActive) glass.textSecondary else Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    if (showRestartDialog) {
        GlassDialog(
            onDismissRequest = { showRestartDialog = false },
            title = "Restart Challenge?",
            actions = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Cancel", color = glass.textSecondary)
                }
                TextButton(
                    onClick = {
                        scope.launch {
                            habitRepository.restartHabit(habit.id)
                            alarmScheduler.rescheduleAllAlarms()
                            showRestartDialog = false
                        }
                    }
                ) {
                    Text("Restart", color = AccentOrange, fontWeight = FontWeight.Bold)
                }
            }
        ) {
            Text(
                text = "This will reset your check-in history and start the ${habit.durationDays}-day countdown fresh from today. This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = glass.textSecondary
            )
        }
    }

    if (showAddReminderSheet) {
        val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
        val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5, 6, 7) }
        val dayLabels = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")

        GlassBottomSheet(onDismissRequest = { showAddReminderSheet = false }) {
            Text(
                text = "Add Reminder Time",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodyMedium,
                color = glass.textSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = timePickerState)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        val newId = reminderRepository.addReminder(
                            habitId = habit.id,
                            timeHour = timePickerState.hour,
                            timeMinute = timePickerState.minute,
                            repeatDays = selectedDays.sorted(),
                            label = "Daily Nudge"
                        )
                        val created = reminderRepository.getReminderById(newId)
                        if (created != null) {
                            alarmScheduler.scheduleReminder(created)
                        }
                        showAddReminderSheet = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(Dimens.radiusPill)
            ) {
                Text("Save Reminder", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StreakMetric(
    label: String,
    value: String,
    valueColor: Color
) {
    val glass = LocalGlassPalette.current
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = glass.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = valueColor
        )
    }
}
