package com.example.pagovoz.ui.theme

import android.app.Activity
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

// Paleta de colores fija para que la app se vea igual en modo claro y oscuro
private val FixedColorScheme = lightColorScheme(
    primary = YapePurple,
    onPrimary = Color.White,
    primaryContainer = YapePurple.copy(alpha = 0.1f),
    onPrimaryContainer = YapePurple,
    secondary = YapeCyan,
    onSecondary = YapePurple,
    background = Color(0xFFF8F9FA), // Gris muy claro para el fondo general
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White, // Blanco puro para las tarjetas
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

@Composable
fun PagoVozTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    // Forzamos el uso de FixedColorScheme para que no cambie con el sistema
    val colorScheme = FixedColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}