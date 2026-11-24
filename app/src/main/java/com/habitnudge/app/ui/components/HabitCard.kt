package com.habitnudge.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.habitnudge.app.domain.model.HabitWithProgress

@Composable
fun HabitCard(
    habitWithProgress: HabitWithProgress,
    onToggleCheckIn: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassHabitCard(
        habitWithProgress = habitWithProgress,
        onToggleCheckIn = onToggleCheckIn,
        onClick = onClick,
        modifier = modifier
    )
}
