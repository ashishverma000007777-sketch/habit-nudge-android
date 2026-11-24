package com.habitnudge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.habitnudge.app.ui.theme.LocalGlassPalette
import com.habitnudge.app.ui.theme.SuccessGreen

@Composable
fun WeeklyBarChart(
    days: List<DayCompletion>,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassPalette.current

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        tier = GlassTier.Medium,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = day.dayOfWeekLetter,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = if (day.isToday) AccentOrange else glass.textSecondary
                    )

                    val indicatorColor = when {
                        day.isCompleted -> SuccessGreen
                        day.isToday -> AccentOrange.copy(alpha = 0.24f)
                        day.isFuture -> glass.surfaceLow
                        else -> glass.surfaceHigh
                    }

                    val borderColor = when {
                        day.isCompleted -> SuccessGreen.copy(alpha = 0.7f)
                        day.isToday -> AccentOrange
                        else -> glass.borderSubtle
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                            .border(if (day.isToday) 1.5.dp else 1.dp, borderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day.isCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .background(Color.White, CircleShape)
                            )
                        } else if (day.isToday) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AccentOrange, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}
