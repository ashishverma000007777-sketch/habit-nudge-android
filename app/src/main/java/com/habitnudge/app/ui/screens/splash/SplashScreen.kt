package com.habitnudge.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.R
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.ui.components.FrostedGlassCard
import com.habitnudge.app.ui.components.GlassBackdrop
import com.habitnudge.app.ui.components.GlassTier
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.LocalGlassPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    preferencesRepository: UserPreferencesRepository,
    onNavigate: (String) -> Unit
) {
    val glass = LocalGlassPalette.current
    val scale = remember { Animatable(0.90f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        delay(950)

        val settings = preferencesRepository.userSettingsFlow.first()
        if (settings.isOnboardingCompleted) {
            onNavigate(Screen.Home.route)
        } else {
            onNavigate(Screen.Onboarding.route)
        }
    }

    GlassBackdrop {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                FrostedGlassCard(
                    tier = GlassTier.High,
                    shape = RoundedCornerShape(34.dp),
                    accentBorderColor = AccentOrange.copy(alpha = 0.35f),
                    contentPadding = PaddingValues(18.dp),
                    shadowElevation = 16.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_habit_nudge_logo),
                        contentDescription = "Habit Nudge Logo",
                        modifier = Modifier.size(92.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Habit Nudge",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    ),
                    color = glass.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Small Habits. Stronger You.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = AccentOrange
                )
            }
        }
    }
}
