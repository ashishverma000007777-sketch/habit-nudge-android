package com.habitnudge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.local.entity.ReminderEntity
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette
import com.habitnudge.app.ui.theme.WarningGold
import java.util.Locale

/**
 * Frosted glass reminder card displaying time, repeat cadence, sound/vibration status,
 * quick snooze controls, toggle switch, and clear active vs inactive differentiation.
 */
@Composable
fun GlassReminderCard(
    reminder: ReminderEntity,
    habitTitle: String,
    soundStatusLabel: String,
    vibrationEnabled: Boolean,
    isRepeatingAlarm: Boolean,
    onToggle: (Boolean) -> Unit,
    onSnooze: ((Int) -> Unit)? = null,
    onClearSnooze: (() -> Unit)? = null,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassPalette.current
    val timeFormatted = String.format(Locale.US, "%02d:%02d", reminder.timeHour, reminder.timeMinute)
    val isSnoozed = reminder.snoozeUntilMillis != null && reminder.snoozeUntilMillis > System.currentTimeMillis()
    val scheduleText = formatRepeatSchedule(reminder.repeatDaysCsv)

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (reminder.isEnabled) 1f else 0.68f),
        tier = if (reminder.isEnabled) GlassTier.Medium else GlassTier.Low,
        accentBorderColor = when {
            isSnoozed && reminder.isEnabled -> WarningGold.copy(alpha = 0.5f)
            reminder.isEnabled -> AccentOrange.copy(alpha = 0.35f)
            else -> null
        },
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (reminder.isEnabled) AccentOrange.copy(alpha = 0.14f)
                            else glass.surfaceLow
                        )
                        .border(
                            Dimens.strokeThin,
                            if (reminder.isEnabled) AccentOrange.copy(alpha = 0.35f) else glass.borderSubtle,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (reminder.isEnabled) Icons.Default.AccessTime else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (reminder.isEnabled) AccentOrange else glass.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (reminder.isEnabled) glass.textPrimary else glass.textSecondary
                        )

                        if (isSnoozed) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.radiusPill))
                                    .background(WarningGold.copy(alpha = 0.16f))
                                    .border(Dimens.strokeThin, WarningGold.copy(alpha = 0.35f), RoundedCornerShape(Dimens.radiusPill))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = "Snoozed",
                                    tint = WarningGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Snoozed",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = WarningGold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = buildString {
                            append(habitTitle)
                            val customLabel = reminder.label?.takeIf { it.isNotBlank() && it != habitTitle }
                            if (customLabel != null) {
                                append(" • ")
                                append(customLabel)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                GlassToggle(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggle
                )

                Spacer(modifier = Modifier.width(6.dp))

                GlassIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    onClick = onDelete,
                    size = 40.dp,
                    iconSize = 18.dp,
                    tint = glass.textSecondary
                )
            }

            // Metadata + Snooze controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Repeat schedule & sound/vibration status pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    StatusChip(text = scheduleText)
                    StatusChip(
                        text = soundStatusLabel,
                        icon = if (isRepeatingAlarm) Icons.Default.Alarm else Icons.AutoMirrored.Filled.VolumeUp
                    )
                    if (vibrationEnabled) {
                        StatusChip(
                            text = "Vib",
                            icon = Icons.Default.Vibration
                        )
                    }
                }

                // Quick Snooze Controls when active
                if (reminder.isEnabled && (onSnooze != null || onClearSnooze != null)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSnoozed && onClearSnooze != null) {
                            SnoozeChip(label = "Clear", highlighted = true, onClick = onClearSnooze)
                        } else if (onSnooze != null) {
                            SnoozeChip(label = "+10m", highlighted = false, onClick = { onSnooze(10) })
                            SnoozeChip(label = "+30m", highlighted = false, onClick = { onSnooze(30) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val glass = LocalGlassPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(glass.surfaceLow)
            .border(Dimens.strokeThin, glass.borderSubtle, RoundedCornerShape(Dimens.radiusPill))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = glass.textSecondary,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = glass.textSecondary
        )
    }
}

@Composable
private fun SnoozeChip(
    label: String,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val glass = LocalGlassPalette.current
    val bg = if (highlighted) WarningGold.copy(alpha = 0.16f) else glass.surfaceHigh
    val border = if (highlighted) WarningGold.copy(alpha = 0.4f) else glass.borderSubtle
    val textColor = if (highlighted) WarningGold else AccentOrange

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(bg)
            .border(Dimens.strokeThin, border, RoundedCornerShape(Dimens.radiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
            color = textColor
        )
    }
}

fun formatRepeatSchedule(repeatDaysCsv: String): String {
    val days = repeatDaysCsv.split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .toSet()

    return when {
        days.isEmpty() || days.size == 7 -> "Daily"
        days == setOf(1, 2, 3, 4, 5) -> "Weekdays"
        days == setOf(6, 7) -> "Weekends"
        else -> {
            val names = mapOf(
                1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu",
                5 to "Fri", 6 to "Sat", 7 to "Sun"
            )
            days.sorted().mapNotNull { names[it] }.joinToString(", ")
        }
    }
}
