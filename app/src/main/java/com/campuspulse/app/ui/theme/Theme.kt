package com.campuspulse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CampusPulseColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Ink,
    secondary = PageNavy,
    onSecondary = PageTextLight,
    background = Surface,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = CardTint,
    onSurfaceVariant = Muted,
    outline = Line,
    error = CategoryAdvocacy
)

@Composable
fun CampusPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            it.statusBarColor = PageNavy.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = CampusPulseColorScheme,
        typography = CampusPulseTypography,
        content = content
    )
}
