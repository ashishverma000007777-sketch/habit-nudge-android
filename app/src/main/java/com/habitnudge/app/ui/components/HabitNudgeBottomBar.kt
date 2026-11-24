package com.habitnudge.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HabitNudgeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassNavigationBar(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        modifier = modifier
    )
}
