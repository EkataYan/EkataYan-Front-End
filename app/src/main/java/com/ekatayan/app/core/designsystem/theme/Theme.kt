package com.ekatayan.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = EkataCardBackground,
    primaryContainer = EkataLightBlue,
    onPrimaryContainer = EkataBlueDark,
    secondary = EkataBlueDark,
    onSecondary = EkataCardBackground,
    secondaryContainer = EkataLightBlue,
    onSecondaryContainer = EkataBlueDark,
    tertiary = EkataBlue,
    onTertiary = EkataCardBackground,
    tertiaryContainer = EkataLightBlue,
    onTertiaryContainer = EkataBlueDark,
    background = EkataBackground,
    surface = EkataCardBackground,
    surfaceVariant = EkataNavigationBackground,
    onBackground = EkataTextPrimary,
    onSurface = EkataTextPrimary,
    onSurfaceVariant = EkataTextSecondary,
    outline = EkataOutline,
    outlineVariant = EkataOutlineStrong,
    error = EkataError,
    errorContainer = EkataErrorContainer,
    inversePrimary = EkataBlue,
    inverseSurface = EkataTextPrimary,
    inverseOnSurface = EkataBackground,
    surfaceTint = EkataBlue,
)
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = EkataDarkBackground,
    secondary = DarkPrimary,
    onSecondary = EkataDarkBackground,
    tertiary = DarkPrimary,
    onTertiary = EkataDarkBackground,
    primaryContainer = EkataDarkSurfaceVariant,
    onPrimaryContainer = DarkPrimary,
    secondaryContainer = EkataDarkSurfaceVariant,
    onSecondaryContainer = DarkPrimary,
    tertiaryContainer = EkataDarkSurfaceVariant,
    onTertiaryContainer = DarkPrimary,
    background = EkataDarkBackground,
    surface = EkataDarkSurface,
    surfaceVariant = EkataDarkSurfaceVariant,
    onBackground = EkataDarkTextPrimary,
    onSurface = EkataDarkTextPrimary,
    onSurfaceVariant = EkataDarkTextSecondary,
    inversePrimary = DarkPrimary,
    inverseSurface = EkataDarkTextPrimary,
    inverseOnSurface = EkataDarkBackground,
    surfaceTint = DarkPrimary,
)

private val EkataYanShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(EkataRadius.small),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(EkataRadius.medium),
    large = androidx.compose.foundation.shape.RoundedCornerShape(EkataRadius.large),
)

@Composable
fun EkataYanTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = EkataYanTypography,
        shapes = EkataYanShapes,
        content = content,
    )
}
