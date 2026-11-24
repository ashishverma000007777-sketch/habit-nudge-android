package com.habitnudge.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette

enum class GlassTier {
    Low,
    Medium,
    High
}

/**
 * Reusable frosted glass surface with thin specular border, restrained elevation,
 * and optional spring press feedback. Avoids per-card RenderEffect blur to preserve
 * crisp text contrast and 60fps scroll performance.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    tier: GlassTier = GlassTier.Medium,
    shape: Shape = RoundedCornerShape(Dimens.radiusMedium),
    accentBorderColor: Color? = null,
    tintColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    shadowElevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 480f),
        label = "glassCardScale"
    )

    val baseSurface = tintColor ?: when (tier) {
        GlassTier.Low -> glass.surfaceLow
        GlassTier.Medium -> glass.surfaceMedium
        GlassTier.High -> glass.surfaceHigh
    }

    val topBorder = accentBorderColor?.copy(alpha = 0.55f) ?: glass.borderHighlight
    val bottomBorder = accentBorderColor?.copy(alpha = 0.22f) ?: glass.borderSubtle

    val cardModifier = modifier
        .scale(scale)
        .then(
            if (shadowElevation > 0.dp) {
                Modifier.shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = glass.shadowTint,
                    spotColor = glass.shadowTint
                )
            } else Modifier
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    baseSurface,
                    baseSurface.copy(alpha = (baseSurface.alpha * 0.88f).coerceIn(0f, 1f))
                )
            )
        )
        .border(
            width = Dimens.strokeThin,
            brush = Brush.verticalGradient(colors = listOf(topBorder, bottomBorder)),
            shape = shape
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
        .padding(contentPadding)

    Box(modifier = cardModifier, content = content)
}
