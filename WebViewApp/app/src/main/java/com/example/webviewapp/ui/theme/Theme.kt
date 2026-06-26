package com.example.webviewapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandNavy80,
    secondary = BrandTeal80,
    tertiary = BrandCoral80,
    error = BrandError
)

private val LightColorScheme = lightColorScheme(
    primary = BrandNavy,            // Estructura y foco de los campos
    onPrimary = Color.White,
    secondary = BrandTeal,          // Acento secundario
    onSecondary = Color.White,
    tertiary = BrandCoral,          // Acento / CTA
    onTertiary = Color.White,
    background = NeutralLight,
    surface = NeutralLight,
    onSurface = NeutralDark,
    onSurfaceVariant = NeutralVariant,
    error = BrandError
)

@Composable
fun WebViewAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Marca propia: desactivamos el color dinámico del sistema para respetar la paleta SportHausen.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
