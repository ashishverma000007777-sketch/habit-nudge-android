package com.habitnudge.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.habitnudge.app.data.preferences.AppTheme

@Immutable
data class GlassPalette(
    val isDark: Boolean,
    val canvasTop: Color,
    val canvasBottom: Color,
    val surfaceLow: Color,
    val surfaceMedium: Color,
    val surfaceHigh: Color,
    val borderSubtle: Color,
    val borderHighlight: Color,
    val navBarSurface: Color,
    val dialogSurface: Color,
    val ringTrack: Color,
    val ambientWarmGlow: Color,
    val ambientCoolGlow: Color,
    val shadowTint: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color
)

private val DarkGlassPalette = GlassPalette(
    isDark = true,
    canvasTop = ObsidianBase,
    canvasBottom = DarkBackground,
    surfaceLow = DarkGlassLow,
    surfaceMedium = DarkGlassMedium,
    surfaceHigh = DarkGlassHigh,
    borderSubtle = DarkGlassBorder,
    borderHighlight = DarkGlassHighlight,
    navBarSurface = Color(0xCC16181E),
    dialogSurface = Color(0xEB181A20),
    ringTrack = Color(0x18FFFFFF),
    ambientWarmGlow = Color(0x16FF8A3D),
    ambientCoolGlow = Color(0x0F6A8DFF),
    shadowTint = DarkGlassShadow,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textTertiary = TextTertiaryDark
)

private val LightGlassPalette = GlassPalette(
    isDark = false,
    canvasTop = LightBackground,
    canvasBottom = LightBackgroundSecondary,
    surfaceLow = LightGlassLow,
    surfaceMedium = LightGlassMedium,
    surfaceHigh = LightGlassHigh,
    borderSubtle = LightBorder,
    borderHighlight = LightGlassHighlight,
    navBarSurface = Color(0xDDF7F8FC),
    dialogSurface = Color(0xF5FFFFFF),
    ringTrack = Color(0x1614161B),
    ambientWarmGlow = Color(0x1AFF8A3D),
    ambientCoolGlow = Color(0x1245AAF2),
    shadowTint = LightGlassShadow,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight
)

val LocalGlassPalette = staticCompositionLocalOf { DarkGlassPalette }

private val DarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = ObsidianBase,
    primaryContainer = AccentOrangeSubtle,
    onPrimaryContainer = AccentOrangeLight,
    secondary = AccentOrangeLight,
    onSecondary = ObsidianBase,
    background = ObsidianBase,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkElevatedCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    outlineVariant = DarkGlassMedium,
    error = ErrorRed,
    onError = ObsidianBase
)

private val LightColorScheme = lightColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = AccentOrangeSubtle,
    onPrimaryContainer = AccentOrange,
    secondary = AccentOrangeLight,
    onSecondary = TextPrimaryLight,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightElevatedCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = LightGlassLow,
    error = ErrorRed,
    onError = Color.White
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
    val glassPalette = if (darkTheme) DarkGlassPalette else LightGlassPalette

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalGlassPalette provides glassPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HabitNudgeTypography,
            content = content
        )
    }
}
