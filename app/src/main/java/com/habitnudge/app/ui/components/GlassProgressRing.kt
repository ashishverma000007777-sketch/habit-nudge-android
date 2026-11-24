package com.habitnudge.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.AccentOrangeLight
import com.habitnudge.app.ui.theme.LocalGlassPalette

/**
 * Layered glass progress ring featuring a translucent track, delicate specular rim,
 * smooth progress transition, and tabular percentage readout.
 */
@Composable
fun GlassProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    progressColor: Color = AccentOrange,
    secondaryColor: Color? = null,
    trackColor: Color? = null,
    showLabel: Boolean = true,
    subtitle: String? = null
) {
    val glass = LocalGlassPalette.current
    val resolvedTrack = trackColor ?: glass.ringTrack
    val resolvedSecondary = secondaryColor ?: if (progressColor == AccentOrange) AccentOrangeLight else progressColor

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "glassProgressRing"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)

            // Outer translucent glass track
            drawArc(
                color = resolvedTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            // Subtle inner specular hairline for glass ring depth
            drawCircle(
                color = glass.borderSubtle.copy(alpha = 0.35f),
                radius = (this.size.minDimension - strokePx * 1.9f) / 2f,
                style = Stroke(width = 0.75.dp.toPx())
            )

            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(progressColor, resolvedSecondary, progressColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = stroke
                )
            }
        }

        if (showLabel) {
            val percent = (animatedProgress * 100).toInt()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.23f).sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = glass.textPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = glass.textSecondary
                    )
                }
            }
        }
    }
}
