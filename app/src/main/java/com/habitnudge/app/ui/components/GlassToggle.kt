package com.habitnudge.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette

/**
 * Custom translucent glass toggle with spring-physics thumb travel and restrained
 * orange activation state.
 */
@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val glass = LocalGlassPalette.current
    val interactionSource = remember { MutableInteractionSource() }

    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> glass.surfaceLow
            checked -> AccentOrange.copy(alpha = 0.85f)
            else -> glass.surfaceHigh
        },
        label = "glassToggleTrack"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            checked && enabled -> AccentOrange
            else -> glass.borderSubtle
        },
        label = "glassToggleBorder"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 480f),
        label = "glassToggleThumb"
    )

    val thumbColor = if (checked) Color.White else if (glass.isDark) Color(0xFFE6E8EC) else Color.White

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(trackColor)
            .border(Dimens.strokeThin, borderColor, RoundedCornerShape(Dimens.radiusPill))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .shadow(2.dp, CircleShape, ambientColor = glass.shadowTint, spotColor = glass.shadowTint)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
