package com.habitnudge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.domain.model.HabitWithProgress
import com.habitnudge.app.reminder.HabitNotificationRegistry
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette
import com.habitnudge.app.ui.theme.SuccessGreen
import com.habitnudge.app.ui.theme.WarningGold

/**
 * Glassmorphic habit card featuring habit-specific identity badge & accent,
 * quick completion action, streak pill, next reminder metadata, and progress ring.
 */
@Composable
fun GlassHabitCard(
    habitWithProgress: HabitWithProgress,
    onToggleCheckIn: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassPalette.current
    val habit = habitWithProgress.habit
    val progress = habitWithProgress.progress
    val identity = HabitNotificationRegistry.getIdentity(habit.id, habit.title)

    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.accentColorHex))
    } catch (e: Exception) {
        AccentOrange
    }

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        tier = if (progress.isCompletedToday) GlassTier.Low else GlassTier.Medium,
        accentBorderColor = if (progress.isCompletedToday) SuccessGreen.copy(alpha = 0.45f) else null,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Habit-specific icon badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = if (glass.isDark) 0.16f else 0.14f))
                    .border(
                        width = Dimens.strokeThin,
                        color = accentColor.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = identity.emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main habit copy & metadata
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = glass.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (habit.isPaused) {
                        Box(
                            modifier = Modifier
                                .background(WarningGold.copy(alpha = 0.16f), RoundedCornerShape(Dimens.radiusPill))
                                .border(Dimens.strokeThin, WarningGold.copy(alpha = 0.3f), RoundedCornerShape(Dimens.radiusPill))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Paused",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = WarningGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = habit.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = glass.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StreakBadge(streakDays = progress.currentStreak)

                    Text(
                        text = "${progress.completedDaysCount}/${habit.durationDays}d",
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                        color = glass.textSecondary
                    )

                    if (habitWithProgress.nextReminder != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Next reminder",
                                tint = glass.textTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = habitWithProgress.nextReminder,
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                color = glass.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Progress ring + quick completion control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GlassProgressRing(
                    progress = progress.progressFraction,
                    size = 42.dp,
                    strokeWidth = 4.dp,
                    progressColor = accentColor,
                    showLabel = true
                )
                CompletionButton(
                    isCompleted = progress.isCompletedToday,
                    onToggle = onToggleCheckIn
                )
            }
        }
    }
}
