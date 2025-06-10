package ru.igrakov.utils

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Цветовая схема для светлой темы
val LightColors = lightColorScheme(
    primary = Color(0xFF1E40AF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEEFC),
    onPrimaryContainer = Color(0xFF1E40AF),

    secondary = Color(0xFF64748B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF475569),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E293B),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),

    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF64748B),

    outline = Color(0xFFCBD5E1),

    error = Color(0xFFB00020),
    onError = Color.White
)

// Цветовая схема для темной темы
val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF202124),

    primaryContainer = Color(0xFF3C4043),
    onPrimaryContainer = Color(0xFF8AB4F8),

    secondary = Color(0xFFBCCCDC),
    onSecondary = Color(0xFF202124),

    secondaryContainer = Color(0xFF2A2C2E),
    onSecondaryContainer = Color(0xFFBCCCDC),

    background = Color(0xFF202124),
    onBackground = Color(0xFFE8EAED),

    surface = Color(0xFF2D2F31),
    onSurface = Color(0xFFE8EAED),

    surfaceVariant = Color(0xFF3C4043),
    onSurfaceVariant = Color(0xFFBCCCDC),

    outline = Color(0xFF5F6368),

    error = Color(0xFFCF6679),
    onError = Color(0xFF202124)
)
