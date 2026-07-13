package com.siamoonga.attendance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AttendlyColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoTint,
    onPrimaryContainer = IndigoDark,

    background = AppBackground,
    onBackground = Ink,

    surface = Surface,
    onSurface = Ink,
    surfaceVariant = AppBackground,
    onSurfaceVariant = Muted,

    outline = Line,
    error = Danger,
    onError = Color.White,
)

@Composable
fun AttendlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AttendlyColors,
        typography = Typography,
        content = content
    )
}