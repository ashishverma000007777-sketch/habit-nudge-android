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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.reminder.HabitNotificationIdentity
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.ErrorRed
import com.habitnudge.app.ui.theme.LocalGlassPalette

/**
 * Branded in-app preview of the native Habit Nudge notification inside a frosted
 * glass container, keeping native system notifications untouched and readable.
 */
@Composable
fun GlassNotificationPreview(
    identity: HabitNotificationIdentity,
    repeatingAlarmEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassPalette.current
    val accentColor = try {
        Color(android.graphics.Color.parseColor(identity.accentColorHex))
    } catch (e: Exception) {
        AccentOrange
    }

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        tier = GlassTier.High,
        accentBorderColor = accentColor.copy(alpha = 0.45f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AccentOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚡", fontSize = 10.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Habit Nudge",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• now",
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textTertiary
                    )
                }

                Text(
                    text = identity.category,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = glass.textSecondary
                )
            }

            // Content row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.22f))
                        .border(1.25.dp, accentColor.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = identity.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${identity.emoji} ${identity.title}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = identity.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                }
            }

            // Interactive action chip preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (repeatingAlarmEnabled) {
                    PreviewActionChip(
                        label = "Stop Alarm",
                        bg = ErrorRed.copy(alpha = 0.16f),
                        border = ErrorRed.copy(alpha = 0.45f),
                        textColor = ErrorRed
                    )
                }
                PreviewActionChip(
                    label = "Mark Done",
                    bg = AccentOrange.copy(alpha = 0.18f),
                    border = AccentOrange.copy(alpha = 0.5f),
                    textColor = AccentOrange
                )
                PreviewActionChip(
                    label = "Snooze 10m",
                    bg = glass.surfaceLow,
                    border = glass.borderSubtle,
                    textColor = glass.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PreviewActionChip(
    label: String,
    bg: Color,
    border: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(bg)
            .border(Dimens.strokeThin, border, RoundedCornerShape(Dimens.radiusPill))
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}
