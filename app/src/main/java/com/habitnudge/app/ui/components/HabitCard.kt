package com.habitnudge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Pause
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
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.WarningGold

@Composable
fun HabitCard(
    habitWithProgress: HabitWithProgress,
    onToggleCheckIn: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithProgress.habit
    val progress = habitWithProgress.progress

    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.accentColorHex))
    } catch (e: Exception) {
        AccentOrange
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Check-in Button
            CompletionButton(
                isCompleted = progress.isCompletedToday,
                onToggle = onToggleCheckIn
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (habit.isPaused) {
                        Box(
                            modifier = Modifier
                                .background(WarningGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Paused",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = habit.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom meta row: streak, remaining days, next reminder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StreakBadge(streakDays = progress.currentStreak)

                    Text(
                        text = "${progress.completedDaysCount}/${habit.durationDays}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (habitWithProgress.nextReminder != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Next reminder",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = habitWithProgress.nextReminder,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Circular progress indicator
            CircularProgressRing(
                progress = progress.progressFraction,
                size = 46.dp,
                strokeWidth = 4.5.dp,
                progressColor = accentColor,
                showLabel = true
            )
        }
    }
}
