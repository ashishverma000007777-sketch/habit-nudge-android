package com.habitnudge.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.local.entity.ReminderEntity
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.data.repository.ReminderRepository
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.ui.components.CircularProgressRing
import com.habitnudge.app.ui.components.CompletionButton
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.components.StreakBadge
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.SuccessGreen
import com.habitnudge.app.ui.theme.WarningGold
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    habitRepository: HabitRepository,
    reminderRepository: ReminderRepository,
    alarmScheduler: AlarmScheduler,
    onBack: () -> Unit
) {
    val habitWithProgress by habitRepository.getHabitWithProgress(habitId).collectAsState(initial = null)
    val reminders by reminderRepository.getRemindersForHabit(habitId).collectAsState(initial = emptyList())
    val checkIns by habitRepository.getCheckInsForHabit(habitId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showRestartDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    val item = habitWithProgress ?: return
    val habit = item.habit
    val progress = item.progress

    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.accentColorHex))
    } catch (e: Exception) {
        AccentOrange
    }

    val startDateFormatted = habit.startDate?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } ?: "Not started"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit.title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
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
            // Header card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = habit.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = accentColor
                            )

                            // Status badge
                            val (statusText, statusColor) = when {
                                !habit.isActive -> "Inactive" to MaterialTheme.colorScheme.onSurfaceVariant
                                habit.isPaused -> "Paused" to WarningGold
                                progress.isChallengeComplete -> "Completed" to SuccessGreen
                                else -> "Active" to SuccessGreen
                            }
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(Dimens.radiusPill))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = statusColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = habit.tagline,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Started: $startDateFormatted • Target: ${habit.durationDays} Days",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor
                        )
                    }
                }
            }

            // Stats grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Progress ring card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressRing(
                                progress = progress.progressFraction,
                                size = 68.dp,
                                strokeWidth = 7.dp,
                                progressColor = accentColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${progress.completedDaysCount} of ${habit.durationDays} Days",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${progress.remainingDays} days remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Streak & Stats card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT STREAK",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${progress.currentStreak} Days",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = WarningGold
                                )
                            }
                            Column {
                                Text(
                                    text = "BEST STREAK",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${progress.bestStreak} Days",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Column {
                                Text(
                                    text = "MISSED DAYS",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${progress.missedDaysCount} Days",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Challenge Tasks checklist
            item {
                SectionHeader(
                    title = "Challenge Directives",
                    subtitle = "Actionable rules for this challenge"
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    habit.tasks.forEach { task ->
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp)
                                    .background(accentColor.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = task,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // 14-day Check-in History Strip
            item {
                SectionHeader(
                    title = "Recent Check-in History",
                    subtitle = "Past 14 days activity"
                )
                Spacer(modifier = Modifier.height(4.dp))
                val today = java.time.LocalDate.now()
                val completedDates = checkIns.filter { it.completed }.map { it.date }.toSet()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (13 downTo 0).forEach { daysAgo ->
                        val date = today.minusDays(daysAgo.toLong())
                        val dateStr = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                        val isDone = completedDates.contains(dateStr)
                        val isToday = daysAgo == 0

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = date.dayOfWeek.name.take(1),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isToday) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) SuccessGreen else if (isToday) AccentOrange.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant)
                                    .then(if (isToday) Modifier.border(1.dp, AccentOrange, CircleShape) else Modifier)
                            )
                        }
                    }
                }
            }

            // Today's Action Toggle
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(if (progress.isCompletedToday) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            Dimens.strokeThin,
                            if (progress.isCompletedToday) SuccessGreen else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(Dimens.radiusMedium)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (progress.isCompletedToday) "Completed for Today! 🎉" else "Mark Today's Nudge",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (progress.isCompletedToday) SuccessGreen else MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (progress.isCompletedToday) "Tap to uncheck if recorded by mistake" else "Tap checkmark to record today's completion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        CompletionButton(
                            isCompleted = progress.isCompletedToday,
                            onToggle = {
                                scope.launch {
                                    habitRepository.toggleCheckInToday(habit.id)
                                }
                            }
                        )
                    }
                }
            }

            // Reminders Section
            item {
                SectionHeader(
                    title = "Scheduled Nudges",
                    subtitle = "Configured notification alerts",
                    actionText = "+ Add Time",
                    onActionClick = { showAddReminderDialog = true }
                )
            }

            if (reminders.isEmpty()) {
                item {
                    Text(
                        text = "No reminders configured yet. Add one to stay accountable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(reminders, key = { it.id }) { reminder ->
                    val timeFormatted = String.format(Locale.US, "%02d:%02d", reminder.timeHour, reminder.timeMinute)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (reminder.isEnabled) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = reminder.label ?: "Daily Nudge",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = reminder.isEnabled,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        reminderRepository.toggleReminder(reminder.id, checked)
                                        if (checked) {
                                            alarmScheduler.scheduleReminder(reminder.copy(isEnabled = true))
                                        } else {
                                            alarmScheduler.cancelReminder(reminder.id)
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.background,
                                    checkedTrackColor = AccentOrange
                                )
                            )
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        alarmScheduler.cancelReminder(reminder.id)
                                        reminderRepository.deleteReminder(reminder.id)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Controls: Pause / Resume, Restart, Deactivate
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(title = "Challenge Controls")
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pause/Resume button
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                habitRepository.setHabitPaused(habit.id, !habit.isPaused)
                                alarmScheduler.rescheduleAllAlarms()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dimens.radiusPill)
                    ) {
                        Icon(
                            imageVector = if (habit.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (habit.isPaused) "Resume" else "Pause")
                    }

                    // Restart button
                    OutlinedButton(
                        onClick = { showRestartDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dimens.radiusPill)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restart")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Activate / Deactivate button
                Button(
                    onClick = {
                        scope.launch {
                            val newActive = !habit.isActive
                            habitRepository.setHabitActive(habit.id, newActive)
                            alarmScheduler.rescheduleAllAlarms()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (habit.isActive) MaterialTheme.colorScheme.surfaceVariant else AccentOrange
                    ),
                    shape = RoundedCornerShape(Dimens.radiusPill)
                ) {
                    Text(
                        text = if (habit.isActive) "Deactivate Challenge" else "Activate Challenge",
                        color = if (habit.isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirmation dialog for restarting challenge
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Restart Challenge?") },
            text = { Text("This will reset your check-in history and start the ${habit.durationDays}-day countdown fresh from today. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            habitRepository.restartHabit(habit.id)
                            alarmScheduler.rescheduleAllAlarms()
                            showRestartDialog = false
                        }
                    }
                ) {
                    Text("Restart", color = AccentOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add reminder dialog
    if (showAddReminderDialog) {
        val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text("Set Reminder Time") },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val newId = reminderRepository.addReminder(
                                habitId = habit.id,
                                timeHour = timePickerState.hour,
                                timeMinute = timePickerState.minute,
                                label = "Daily Nudge"
                            )
                            val created = reminderRepository.getReminderById(newId)
                            if (created != null) {
                                alarmScheduler.scheduleReminder(created)
                            }
                            showAddReminderDialog = false
                        }
                    }
                ) {
                    Text("Save", color = AccentOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
