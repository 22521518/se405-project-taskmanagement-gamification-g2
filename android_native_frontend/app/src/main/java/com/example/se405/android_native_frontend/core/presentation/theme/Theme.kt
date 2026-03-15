package com.example.se405.android_native_frontend.core.presentation.theme

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
    // primary group
    primary = Blue90,            // pallet2-1st
    onPrimary = Blue40,          // pallet2-accent
    primaryContainer = White,

    // secondary group
    secondary = Blue80,          // pallet2-2nd
    secondaryContainer = BlueGrey70, // pallet-2nd-2nd
    onSecondary = Blue40,

    // accent / tertiary
    tertiary = Blue60,           // pallet2-2nd-accent
    onTertiary = White,

    // background / surface
    background = Blue90,
    surface = BlueGrey80,
    onBackground = Blue40,
    onSurface = Blue40,

    // extra accent
    outline = Gold40
)

private val LightColorScheme = lightColorScheme(

    // primary group
    primary = White,            // white
    onPrimary = Blue40,          // pallet2-accent
    primaryContainer = Blue90,   // pallet2-1st

    // secondary group
    secondary = Blue80,          // pallet2-2nd
    secondaryContainer = BlueGrey70, // pallet-2nd-2nd
        onSecondary = Blue40,

    // accent / tertiary
    tertiary = Blue60,           // pallet2-2nd-accent
    onTertiary = White,

    // background / surface
    background = Blue90,
    surface = BlueGrey80,       // pallet2-grey
    onBackground = Blue40,
    onSurface = Blue40,

    // extra accent
    outline = Gold40        // pallet2-contrast-accent
)

@Composable
fun Android_native_frontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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