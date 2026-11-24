package com.habitnudge.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette

/**
 * Translucent icon button with 48dp minimum touch target, specular border,
 * and subtle spring press animation.
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.minTouchTarget,
    iconSize: Dp = 20.dp,
    shape: Shape = CircleShape,
    tint: Color? = null,
    accentBorder: Color? = null,
    enabled: Boolean = true
) {
    val glass = LocalGlassPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f),
        label = "glassIconScale"
    )

    val resolvedTint = tint ?: glass.textPrimary
    val borderTop = accentBorder?.copy(alpha = 0.55f) ?: glass.borderHighlight
    val borderBottom = accentBorder?.copy(alpha = 0.2f) ?: glass.borderSubtle

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(shape)
            .background(glass.surfaceMedium)
            .border(
                width = Dimens.strokeThin,
                brush = Brush.verticalGradient(listOf(borderTop, borderBottom)),
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) resolvedTint else glass.textTertiary,
            modifier = Modifier.size(iconSize)
        )
    }
}
