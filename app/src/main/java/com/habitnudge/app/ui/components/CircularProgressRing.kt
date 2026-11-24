package com.habitnudge.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.LocalGlassPalette

@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    progressColor: Color = AccentOrange,
    trackColor: Color = LocalGlassPalette.current.ringTrack,
    showLabel: Boolean = true
) {
    GlassProgressRing(
        progress = progress,
        modifier = modifier,
        size = size,
        strokeWidth = strokeWidth,
        progressColor = progressColor,
        trackColor = trackColor,
        showLabel = showLabel
    )
}
