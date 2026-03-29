package com.example.praktam_2417051072.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = OrangeSecondary,
    background = CreamBackground,
    surface = CardSurface,
    onPrimary = OnPrimaryText
)

@Composable
fun PrakTAM_2417051072Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content
    )
}
