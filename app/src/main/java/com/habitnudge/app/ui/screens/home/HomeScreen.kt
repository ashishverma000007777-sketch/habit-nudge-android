package com.habitnudge.app.ui.screens.home

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.domain.model.OverallDailyStats
import com.habitnudge.app.ui.components.EmptyStateCard
import com.habitnudge.app.ui.components.FrostedGlassCard
import com.habitnudge.app.ui.components.GlassBackdrop
import com.habitnudge.app.ui.components.GlassHabitCard
import com.habitnudge.app.ui.components.GlassIconButton
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    habitRepository: HabitRepository,
    onNavigate: (String) -> Unit
) {
    val glass = LocalGlassPalette.current
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

    GlassBackdrop {
        Scaffold(
            bottomBar = {
                GlassNavigationBar(
                    currentRoute = Screen.Home.route,
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
                // Top Header: Greeting, Date, Streak Pill, Settings Control
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dateFormatted.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                                color = glass.textSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 27.sp
                                ),
                                color = glass.textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.radiusPill))
                                    .background(WarningGold.copy(alpha = 0.14f))
                                    .border(
                                        Dimens.strokeThin,
                                        WarningGold.copy(alpha = 0.35f),
                                        RoundedCornerShape(Dimens.radiusPill)
                                    )
                                    .padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Active streak days",
                                    tint = WarningGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${overallStats.activeStreaksSum}d",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = WarningGold
                                )
                            }

                            GlassIconButton(
                                icon = Icons.Default.Tune,
                                contentDescription = "Profile and Settings",
                                onClick = { onNavigate(Screen.Settings.route) },
                                size = 42.dp,
                                iconSize = 19.dp
                            )
                        }
                    }
                }

                // Hero Glass Progress Panel
                item {
                    HomeDailyProgressCard(
                        stats = overallStats,
                        onViewReminders = { onNavigate(Screen.Reminders.route) }
                    )
                }

                // Weekly Momentum
                item {
                    SectionHeader(
                        title = "This Week's Momentum",
                        subtitle = "Consistency builds lasting clarity"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    WeeklyBarChart(days = weeklyDays)
                }

                // Active Habits
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
                            GlassHabitCard(
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
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionHeader(
                                title = "Completed Today (${completedHabits.size})",
                                subtitle = "Quietly compounding progress"
                            )
                        }
                        items(completedHabits, key = { it.habit.id }) { item ->
                            GlassHabitCard(
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

                // Night Journal Reflection Glass Banner
                item {
                    HomeJournalBanner(
                        onWriteJournal = { onNavigate(Screen.Journal.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeDailyProgressCard(
    stats: OverallDailyStats,
    onViewReminders: () -> Unit
) {
    val glass = LocalGlassPalette.current
    val progress = if (stats.totalActiveHabits > 0) {
        stats.completedTodayCount.toFloat() / stats.totalActiveHabits
    } else 0f

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        tier = GlassTier.High,
        shape = RoundedCornerShape(Dimens.radiusLarge),
        accentBorderColor = if (stats.completionPercentage == 100) SuccessGreen.copy(alpha = 0.45f) else AccentOrange.copy(alpha = 0.28f),
        contentPadding = PaddingValues(22.dp),
        shadowElevation = 10.dp,
        onClick = onViewReminders
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TODAY'S COMPLETION",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = glass.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stats.completedTodayCount} of ${stats.totalActiveHabits} Habits",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = glass.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        stats.totalActiveHabits == 0 -> "Tap to configure your daily nudges"
                        stats.completionPercentage == 100 -> "Every habit complete today. Rest well."
                        stats.completedTodayCount > 0 -> "Steady rhythm — keep showing up."
                        else -> "Begin with one mindful action."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (stats.completionPercentage == 100) SuccessGreen else glass.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.radiusPill))
                        .background(glass.surfaceLow)
                        .border(Dimens.strokeThin, glass.borderSubtle, RoundedCornerShape(Dimens.radiusPill))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nudge Schedule",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = glass.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            GlassProgressRing(
                progress = progress,
                size = 108.dp,
                strokeWidth = 9.dp,
                progressColor = if (stats.completionPercentage == 100) SuccessGreen else AccentOrange,
                subtitle = "today"
            )
        }
    }
}

@Composable
private fun HomeJournalBanner(
    onWriteJournal: () -> Unit
) {
    val glass = LocalGlassPalette.current

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        tier = GlassTier.Medium,
        accentBorderColor = AccentOrange.copy(alpha = 0.35f),
        contentPadding = PaddingValues(18.dp),
        onClick = onWriteJournal
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
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "3 quiet prompts to close today and anchor tomorrow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glass.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.radiusPill))
                    .background(AccentOrange.copy(alpha = 0.16f))
                    .border(Dimens.strokeThin, AccentOrange.copy(alpha = 0.4f), RoundedCornerShape(Dimens.radiusPill))
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Reflect",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
