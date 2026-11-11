package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedTealPrimaryDark,
    onPrimary = MedTealOnPrimaryDark,
    primaryContainer = MedTealContainerDark,
    onPrimaryContainer = MedTealOnContainerDark,
    secondary = MedGreenSecondaryDark,
    onSecondary = MedGreenOnSecondaryDark,
    secondaryContainer = MedGreenContainerDark,
    onSecondaryContainer = MedGreenOnContainerDark,
    tertiary = MedRedEmergencyDark,
    tertiaryContainer = MedRedContainerDark,
    onTertiaryContainer = MedRedOnContainerDark,
    background = MedBackgroundDark,
    surface = MedSurfaceDark,
    surfaceVariant = MedSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = MedTealPrimary,
    onPrimary = MedTealOnPrimary,
    primaryContainer = MedTealContainer,
    onPrimaryContainer = MedTealOnContainer,
    secondary = MedGreenSecondary,
    onSecondary = MedGreenOnSecondary,
    secondaryContainer = MedGreenContainer,
    onSecondaryContainer = MedGreenOnContainer,
    tertiary = MedRedEmergency,
    tertiaryContainer = MedRedContainer,
    onTertiaryContainer = MedRedOnContainer,
    background = MedBackgroundLight,
    surface = MedSurfaceLight,
    surfaceVariant = MedSurfaceVariantLight
)

@Composable
fun MedAssistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false so custom cohesive medical branding shines
    content: @Composable () -> Unit,
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
