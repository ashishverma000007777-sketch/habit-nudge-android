package com.habitnudge.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.domain.model.OverallDailyStats
import com.habitnudge.app.reminder.HabitNotificationRegistry
import com.habitnudge.app.ui.components.EmptyStateCard
import com.habitnudge.app.ui.components.FrostedGlassCard
import com.habitnudge.app.ui.components.GlassBackdrop
import com.habitnudge.app.ui.components.GlassNavigationBar
import com.habitnudge.app.ui.components.GlassProgressRing
import com.habitnudge.app.ui.components.GlassTier
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.components.WeeklyBarChart
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette
import com.habitnudge.app.ui.theme.SuccessGreen
import com.habitnudge.app.ui.theme.WarningGold

@Composable
fun ProgressStatsScreen(
    habitRepository: HabitRepository,
    onNavigate: (String) -> Unit
) {
    val glass = LocalGlassPalette.current
    val activeHabitsWithProgress by habitRepository.getActiveHabitsWithProgress().collectAsState(initial = emptyList())
    val overallStats by habitRepository.getOverallDailyStats().collectAsState(
        initial = OverallDailyStats(0, 0, 0, 0, 0, 0)
    )
    val weeklyDays by habitRepository.getWeeklyActivity().collectAsState(initial = emptyList())

    GlassBackdrop {
        Scaffold(
            bottomBar = {
                GlassNavigationBar(
                    currentRoute = Screen.Stats.route,
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
                            text = "ANALYTICS & DISCIPLINE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = glass.textSecondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Progress & Stats",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 27.sp
                            ),
                            color = glass.textPrimary
                        )
                    }
                }

                // 2x2 Frosted Metric Grid
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
                                subtitle = "Combined discipline",
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
                    Spacer(modifier = Modifier.height(6.dp))
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
                        EmptyStateCard(
                            title = "No Active Challenges",
                            description = "Start a habit challenge from Home to inspect detailed streak and completion trends.",
                            icon = Icons.Default.BarChart
                        )
                    }
                } else {
                    items(activeHabitsWithProgress, key = { it.habit.id }) { item ->
                        val habit = item.habit
                        val progress = item.progress
                        val identity = HabitNotificationRegistry.getIdentity(habit.id, habit.title)
                        val accentColor = try {
                            Color(android.graphics.Color.parseColor(habit.accentColorHex))
                        } catch (e: Exception) {
                            AccentOrange
                        }

                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            tier = GlassTier.Medium,
                            contentPadding = PaddingValues(16.dp),
                            onClick = { onNavigate(Screen.HabitDetail.createRoute(habit.id)) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassProgressRing(
                                    progress = progress.progressFraction,
                                    size = 54.dp,
                                    strokeWidth = 5.dp,
                                    progressColor = accentColor
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = identity.emoji, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = habit.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = glass.textPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${progress.completedDaysCount} of ${habit.durationDays} days completed",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = glass.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "Streak: ${progress.currentStreak}d",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                            color = WarningGold
                                        )
                                        Text(
                                            text = "Best: ${progress.bestStreak}d",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                            color = glass.textSecondary
                                        )
                                        Text(
                                            text = "Missed: ${progress.missedDaysCount}d",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                            color = glass.textTertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
    tint: Color,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassPalette.current

    FrostedGlassCard(
        modifier = modifier,
        tier = GlassTier.Medium,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                    color = glass.textSecondary
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(tint.copy(alpha = 0.14f))
                        .border(Dimens.strokeThin, tint.copy(alpha = 0.3f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = glass.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = glass.textSecondary
            )
        }
    }
}
