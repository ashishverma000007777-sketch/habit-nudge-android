package com.habitnudge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.habitnudge.app.domain.model.DayCompletion
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.SuccessGreen

@Composable
fun WeeklyBarChart(
    days: List<DayCompletion>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(Dimens.radiusMedium))
            .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Day letter
                Text(
                    text = day.dayOfWeekLetter,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = if (day.isToday) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Dot or Bar
                val indicatorColor = when {
                    day.isCompleted -> SuccessGreen
                    day.isToday -> AccentOrange.copy(alpha = 0.35f)
                    day.isFuture -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                        .then(
                            if (day.isToday) Modifier.border(1.5.dp, AccentOrange, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (day.isCompleted) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.background, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
