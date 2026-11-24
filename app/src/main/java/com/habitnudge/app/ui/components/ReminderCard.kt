package com.habitnudge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.local.entity.ReminderEntity
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.WarningGold
import java.util.Locale

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    habitTitle: String,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = String.format(Locale.US, "%02d:%02d", reminder.timeHour, reminder.timeMinute)
    val isSnoozed = reminder.snoozeUntilMillis != null && reminder.snoozeUntilMillis > System.currentTimeMillis()

    Box(
        modifier = modifier
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
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = if (reminder.isEnabled) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = if (reminder.isEnabled) MaterialTheme.colorScheme.onBackground
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isSnoozed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(WarningGold.copy(alpha = 0.15f), RoundedCornerShape(Dimens.radiusPill))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = "Snoozed",
                                tint = WarningGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Snoozed",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = reminder.label ?: habitTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    checkedTrackColor = AccentOrange
                )
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(Dimens.minTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
