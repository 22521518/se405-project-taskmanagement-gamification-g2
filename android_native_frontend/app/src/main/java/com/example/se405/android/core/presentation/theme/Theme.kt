package com.example.se405.android.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    // primary group
    primary = White,            // white
    onPrimary = Blue40,          // pallet2-accent
    primaryContainer = Blue90,   // pallet2-1st

    // secondary group
    secondary = Blue80,          // pallet2-2nd
    secondaryContainer = BlueGrey70, // pallet-2nd-2nd
    onSecondary = Blue50,

    // accent / tertiary
    tertiary = Blue60,           // pallet2-2nd-accent
    onTertiary = White,

    // background / surface
    background = Color.White,   //Blue90,
    surface = BlueGrey80,       // pallet2-grey
    onBackground = Blue40,
    onSurface = Blue40,

    // extra accent
    outline = Gold40        // pallet2-contrast-accent
)
//
//private val DarkColorScheme = darkColorScheme()

@Composable
fun Android_Theme(
    content: @Composable () -> Unit
) {

    val colorScheme = LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}