package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedDarkPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = MedTealPrimaryContainer,
    secondary = Color(0xFFB1CBD0),
    background = MedDarkBackground,
    surface = MedDarkSurface,
    surfaceVariant = MedDarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = MedTealPrimary,
    onPrimary = MedTealOnPrimary,
    primaryContainer = MedTealPrimaryContainer,
    onPrimaryContainer = MedTealOnPrimaryContainer,
    secondary = MedTealSecondary,
    secondaryContainer = MedTealSecondaryContainer,
    onSecondaryContainer = MedTealOnSecondaryContainer,
    tertiary = MedTealTertiary,
    background = MedLightBackground,
    surface = MedLightSurface,
    surfaceVariant = MedLightSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep medical brand colors consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
