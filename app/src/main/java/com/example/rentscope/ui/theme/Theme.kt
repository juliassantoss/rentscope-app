package com.example.rentscope.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RentScopeLightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = SoftBlueBackground,
    onPrimaryContainer = DeepBlue,

    secondary = DeepBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftBlueBackground,
    onSecondaryContainer = DeepBlue,

    tertiary = DeepBlue,
    onTertiary = Color.White,

    background = Color.White,
    onBackground = OnSurfaceDark,

    surface = SurfaceWhite,
    onSurface = OnSurfaceDark,

    surfaceVariant = SoftBlueBackground,
    onSurfaceVariant = OnSurfaceVariant,

    outline = OutlineVariant,
    outlineVariant = OutlineVariant
)

/**
 * Tema do RentScope.
 *
 * Sempre usa o esquema de cores claro, independentemente das configurações
 * do sistema (modo escuro do telemóvel ou cores dinâmicas do Android 12+).
 */
@Composable
fun RentscopeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RentScopeLightColorScheme,
        typography = Typography,
        content = content
    )
}
