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
    primary = Purple80,
    onPrimary = Color(0xFF381E72),
    primaryContainer = PurpleDark,
    onPrimaryContainer = Purple80,
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Pink80,
    onTertiary = Color(0xFF492532),
    background = NeutralBackgroundDark,
    onBackground = Color(0xFFE6E1E5),
    surface = NeutralSurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = NeutralSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = NeutralBorderDark,
    outlineVariant = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleContainer,
    onPrimaryContainer = PrimaryPurpleDark,
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = PrimaryPurpleSubtle,
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Pink40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = NeutralBackgroundLight,
    onBackground = NeutralTextPrimary,
    surface = NeutralSurfaceLight,
    onSurface = NeutralTextPrimary,
    surfaceVariant = NeutralSurfaceVariantLight,
    onSurfaceVariant = NeutralTextSecondary,
    outline = NeutralBorder,
    outlineVariant = NeutralBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our curated medical palette for consistency
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
