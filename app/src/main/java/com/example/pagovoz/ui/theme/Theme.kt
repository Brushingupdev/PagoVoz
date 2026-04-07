package com.example.pagovoz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val FixedColorScheme = darkColorScheme(
    primary = AppColors.BrandPrimary,
    onPrimary = ColorSchemeValues.OnBrand,
    primaryContainer = AppColors.SurfaceBrand,
    onPrimaryContainer = AppColors.BrandAccent,
    secondary = AppColors.BrandAccent,
    onSecondary = AppColors.TextPrimary,
    secondaryContainer = AppColors.WarningContainer,
    onSecondaryContainer = AppColors.Warning,
    tertiary = AppColors.Success,
    onTertiary = ColorSchemeValues.OnBrand,
    tertiaryContainer = AppColors.SuccessContainer,
    onTertiaryContainer = AppColors.Success,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceMuted,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Border,
    surfaceTint = AppColors.Surface,
    error = AppColors.Error,
    onError = AppColors.Surface,
    errorContainer = AppColors.ErrorContainer,
    onErrorContainer = AppColors.Error
)

private object ColorSchemeValues {
    val OnBrand = AppColors.Background
}

@Composable
fun HablaPagoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    @Suppress("UNUSED_VARIABLE")
    val ignoredDarkTheme = darkTheme
    @Suppress("UNUSED_VARIABLE")
    val ignoredDynamicColor = dynamicColor
    val colorScheme = FixedColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
