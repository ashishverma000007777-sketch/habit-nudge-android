package com.habitnudge.app.ui.screens.stats

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.domain.model.OverallDailyStats
import com.habitnudge.app.ui.components.CircularProgressRing
import com.habitnudge.app.ui.components.HabitNudgeBottomBar
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.components.WeeklyBarChart
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.SuccessGreen
import com.habitnudge.app.ui.theme.WarningGold

@Composable
fun ProgressStatsScreen(
    habitRepository: HabitRepository,
    onNavigate: (String) -> Unit
) {
    val activeHabitsWithProgress by habitRepository.getActiveHabitsWithProgress().collectAsState(initial = emptyList())
    val overallStats by habitRepository.getOverallDailyStats().collectAsState(
        initial = OverallDailyStats(0, 0, 0, 0, 0, 0)
    )
    val weeklyDays by habitRepository.getWeeklyActivity().collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            HabitNudgeBottomBar(
                currentRoute = Screen.Stats.route,
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
                        text = "ANALYTICS & DISCIPLINE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Progress & Stats",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Summary 4-stat metric grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            title = "Daily Completion",
                            value = "${overallStats.completionPercentage}%",
                            subtitle = "${overallStats.completedTodayCount}/${overallStats.totalActiveHabits} habits",
                            icon = Icons.Default.CheckCircle,
                            tint = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            title = "Best Streak",
                            value = "${overallStats.bestOverallStreak} Days",
                            subtitle = "Consecutive record",
                            icon = Icons.Default.LocalFireDepartment,
                            tint = WarningGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            title = "Total Streaks",
                            value = "${overallStats.activeStreaksSum} Days",
                            subtitle = "Combined active discipline",
                            icon = Icons.Default.LocalFireDepartment,
                            tint = AccentOrange,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            title = "Challenges Won",
                            value = "${overallStats.completedChallengesCount}",
                            subtitle = "Milestones achieved",
                            icon = Icons.Default.EmojiEvents,
                            tint = AccentOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Weekly activity
            item {
                SectionHeader(
                    title = "7-Day Activity Rhythm",
                    subtitle = "Day-by-day execution tracking"
                )
                Spacer(modifier = Modifier.height(4.dp))
                WeeklyBarChart(days = weeklyDays)
            }

            // Individual Habit Breakdowns
            item {
                SectionHeader(
                    title = "Active Challenge Breakdowns",
                    subtitle = "Individual progress towards completion"
                )
            }

            if (activeHabitsWithProgress.isEmpty()) {
                item {
                    Text(
                        text = "No active challenges currently being tracked.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(activeHabitsWithProgress, key = { it.habit.id }) { item ->
                    val habit = item.habit
                    val progress = item.progress

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressRing(
                                progress = progress.progressFraction,
                                size = 52.dp,
                                strokeWidth = 5.dp,
                                progressColor = AccentOrange
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = habit.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${progress.completedDaysCount} of ${habit.durationDays} days completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Streak: ${progress.currentStreak}d",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarningGold
                                    )
                                    Text(
                                        text = "Missed: ${progress.missedDaysCount}d",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
