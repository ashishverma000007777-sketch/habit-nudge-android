package com.habitnudge.app.ui.screens.home

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.domain.model.DayCompletion
import com.habitnudge.app.domain.model.HabitWithProgress
import com.habitnudge.app.domain.model.OverallDailyStats
import com.habitnudge.app.ui.components.CircularProgressRing
import com.habitnudge.app.ui.components.EmptyStateCard
import com.habitnudge.app.ui.components.HabitCard
import com.habitnudge.app.ui.components.HabitNudgeBottomBar
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.components.WeeklyBarChart
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.SuccessGreen
import com.habitnudge.app.ui.theme.WarningGold
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    habitRepository: HabitRepository,
    onNavigate: (String) -> Unit
) {
    val activeHabitsWithProgress by habitRepository.getActiveHabitsWithProgress().collectAsState(initial = emptyList())
    val overallStats by habitRepository.getOverallDailyStats().collectAsState(
        initial = OverallDailyStats(0, 0, 0, 0, 0, 0)
    )
    val weeklyDays by habitRepository.getWeeklyActivity().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val pendingHabits = activeHabitsWithProgress.filter { !it.progress.isCompletedToday }
    val completedHabits = activeHabitsWithProgress.filter { it.progress.isCompletedToday }

    val today = LocalDate.now()
    val dateFormatted = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }

    Scaffold(
        bottomBar = {
            HabitNudgeBottomBar(
                currentRoute = Screen.Home.route,
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

                // Greeting & Date Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dateFormatted.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Streak Pill
                    Row(
                        modifier = Modifier
                            .background(WarningGold.copy(alpha = 0.15f), RoundedCornerShape(Dimens.radiusPill))
                            .border(Dimens.strokeThin, WarningGold.copy(alpha = 0.35f), RoundedCornerShape(Dimens.radiusPill))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = WarningGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${overallStats.activeStreaksSum} Streak Days",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = WarningGold
                        )
                    }
                }
            }

            // Daily Progress Overview Hero Card
            item {
                HomeDailyProgressCard(
                    stats = overallStats,
                    onViewReminders = { onNavigate(Screen.Reminders.route) }
                )
            }

            // Weekly Activity Matrix
            item {
                SectionHeader(
                    title = "This Week's Momentum",
                    subtitle = "Consistency builds permanent neural pathways"
                )
                Spacer(modifier = Modifier.height(4.dp))
                WeeklyBarChart(days = weeklyDays)
            }

            // Active / Pending Habits
            if (activeHabitsWithProgress.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Active Challenges",
                        description = "Choose from the 7 curated habit challenges to build consistency and unlock your potential.",
                        icon = Icons.Default.Schedule,
                        actionLabel = "Browse Challenges",
                        onActionClick = { onNavigate(Screen.Onboarding.route) }
                    )
                }
            } else {
                if (pendingHabits.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Today's Focus (${pendingHabits.size})",
                            subtitle = "Small steps you can take today"
                        )
                    }
                    items(pendingHabits, key = { it.habit.id }) { item ->
                        HabitCard(
                            habitWithProgress = item,
                            onToggleCheckIn = {
                                scope.launch {
                                    habitRepository.toggleCheckInToday(item.habit.id)
                                }
                            },
                            onClick = {
                                onNavigate(Screen.HabitDetail.createRoute(item.habit.id))
                            }
                        )
                    }
                }

                if (completedHabits.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "Completed Today (${completedHabits.size})",
                            subtitle = "Well done! You showed up today"
                        )
                    }
                    items(completedHabits, key = { it.habit.id }) { item ->
                        HabitCard(
                            habitWithProgress = item,
                            onToggleCheckIn = {
                                scope.launch {
                                    habitRepository.toggleCheckInToday(item.habit.id)
                                }
                            },
                            onClick = {
                                onNavigate(Screen.HabitDetail.createRoute(item.habit.id))
                            }
                        )
                    }
                }
            }

            // Night Journal Quick Prompt banner
            item {
                HomeJournalBanner(
                    onWriteJournal = { onNavigate(Screen.Journal.route) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun HomeDailyProgressCard(
    stats: OverallDailyStats,
    onViewReminders: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TODAY'S COMPLETION",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${stats.completedTodayCount} of ${stats.totalActiveHabits} Habits",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        stats.totalActiveHabits == 0 -> "Tap to activate your starting habits"
                        stats.completionPercentage == 100 -> "All habits completed for today! 🎯"
                        stats.completedTodayCount > 0 -> "Keep going, great momentum!"
                        else -> "Ready to take your first nudge?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (stats.completionPercentage == 100) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            CircularProgressRing(
                progress = if (stats.totalActiveHabits > 0) stats.completedTodayCount.toFloat() / stats.totalActiveHabits else 0f,
                size = 72.dp,
                strokeWidth = 7.dp,
                progressColor = AccentOrange
            )
        }
    }
}

@Composable
private fun HomeJournalBanner(
    onWriteJournal: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(MaterialTheme.colorScheme.surface)
            .border(Dimens.strokeThin, AccentOrange.copy(alpha = 0.35f), RoundedCornerShape(Dimens.radiusMedium))
            .clickable(onClick = onWriteJournal)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Night Journal Reflection",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AccentOrange
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "3 simple prompts to close today and set tomorrow's focus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onWriteJournal,
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(Dimens.radiusPill)
            ) {
                Text(
                    text = "Reflect",
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentOrange
                )
            }
        }
    }
}
