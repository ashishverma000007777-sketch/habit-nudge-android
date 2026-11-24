package com.habitnudge.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.habitnudge.app.data.preferences.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = DarkBackground,
    primaryContainer = AccentOrangeSubtle,
    onPrimaryContainer = AccentOrangeLight,
    secondary = AccentOrangeLight,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkElevatedCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    outlineVariant = DarkElevatedCard,
    error = ErrorRed,
    onError = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = AccentOrange,
    onPrimary = LightBackground,
    primaryContainer = AccentOrangeSubtle,
    onPrimaryContainer = AccentOrange,
    secondary = AccentOrangeLight,
    onSecondary = LightBackground,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightElevatedCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = LightElevatedCard,
    error = ErrorRed,
    onError = LightBackground
)

@Composable
fun HabitNudgeTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HabitNudgeTypography,
        content = content
    )
}
