package com.valsagnapps.dndapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Paper light, or paper dark following the system. The identity is fixed: no dynamic color.
 * The Material themes of the design system come later, with a settings screen to pick them.
 */
@Composable
fun DnDAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) PaperDarkColorScheme else PaperLightColorScheme,
        typography = Typography,
        content = content,
    )
}
