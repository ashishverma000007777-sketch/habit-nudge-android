package com.habitnudge.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.ui.navigation.BottomNavDestination
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette

/**
 * Translucent floating bottom navigation dock with subtle specular border
 * and spring-animated selection capsule.
 */
@Composable
fun GlassNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassPalette.current
    val dockShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = dockShape,
                    ambientColor = glass.shadowTint,
                    spotColor = glass.shadowTint
                )
                .clip(dockShape)
                .background(glass.navBarSurface)
                .border(
                    width = Dimens.strokeThin,
                    brush = Brush.verticalGradient(
                        colors = listOf(glass.borderHighlight, glass.borderSubtle)
                    ),
                    shape = dockShape
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavDestination.values().forEach { destination ->
                val selected = currentRoute == destination.route
                val interactionSource = remember { MutableInteractionSource() }

                val pillBg by animateColorAsState(
                    targetValue = if (selected) AccentOrange.copy(alpha = 0.16f) else Color.Transparent,
                    label = "navPillBg"
                )
                val pillBorder by animateColorAsState(
                    targetValue = if (selected) AccentOrange.copy(alpha = 0.35f) else Color.Transparent,
                    label = "navPillBorder"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) AccentOrange else glass.textSecondary,
                    label = "navTint"
                )
                val itemScale by animateFloatAsState(
                    targetValue = if (selected) 1.03f else 1f,
                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
                    label = "navScale"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .scale(itemScale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(pillBg)
                        .border(Dimens.strokeThin, pillBorder, RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onNavigate(destination.route) }
                        )
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
