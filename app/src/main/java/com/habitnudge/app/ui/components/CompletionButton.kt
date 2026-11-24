package com.habitnudge.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.SuccessGreen

@Composable
fun CompletionButton(
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.minTouchTarget
) {
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scaleAnimation"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isCompleted) SuccessGreen else Color.Transparent,
        label = "bgColorAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.outline,
        label = "borderColorAnimation"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Checkbox,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size - 12.dp)
                .background(bgColor, CircleShape)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
