package com.habitnudge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.ui.navigation.BottomNavDestination
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens

@Composable
fun HabitNudgeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimens.strokeThin,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(topStart = Dimens.radiusMedium, topEnd = Dimens.radiusMedium)
            )
            .clip(RoundedCornerShape(topStart = Dimens.radiusMedium, topEnd = Dimens.radiusMedium)),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BottomNavDestination.values().forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title,
                        tint = if (selected) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (selected) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = AccentOrange.copy(alpha = 0.12f)
                )
            )
        }
    }
}
