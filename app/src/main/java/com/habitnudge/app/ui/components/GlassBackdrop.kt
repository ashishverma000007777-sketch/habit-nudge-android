package com.habitnudge.app.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.habitnudge.app.ui.theme.LocalGlassPalette

/**
 * Atmospheric canvas behind frosted glass surfaces.
 * Provides restrained warm and cool ambient light sources without neon saturation
 * so translucent glass layers maintain depth and legibility.
 */
@Composable
fun GlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassPalette.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(glass.canvasTop, glass.canvasBottom)
                )
            )
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Hardware-accelerated ambient light orbs only on Android 12+
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 70.dp, y = (-50).dp)
                    .size(280.dp)
                    .blur(90.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(glass.ambientWarmGlow, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-80).dp, y = 60.dp)
                    .size(300.dp)
                    .blur(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(glass.ambientCoolGlow, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        } else {
            // Pre-Android 12 smooth gradient fallback (no expensive software blur)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glass.ambientWarmGlow, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.12f),
                        radius = size.width * 0.72f
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.12f),
                    radius = size.width * 0.72f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glass.ambientCoolGlow, Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.85f),
                        radius = size.width * 0.78f
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.85f),
                    radius = size.width * 0.78f
                )
            }
        }

        content()
    }
}
