package com.habitnudge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.WarningGold

@Composable
fun StreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier,
    highlight: Boolean = streakDays > 0
) {
    val tint = if (highlight) WarningGold else MaterialTheme.colorScheme.onSurfaceVariant
    val bg = if (highlight) WarningGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (highlight) WarningGold.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(Dimens.radiusPill))
            .border(Dimens.strokeThin, borderColor, RoundedCornerShape(Dimens.radiusPill))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Streak",
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streakDays d",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = tint
        )
    }
}
