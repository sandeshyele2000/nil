package com.sandesh.nil.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NilLightColors = lightColorScheme(
    primary = RedPrimary,
    onPrimary = WhitePrimary,
    background = WhitePrimary,
    onBackground = BlackPrimary,
    surface = WhitePrimary,
    onSurface = BlackPrimary
)

private val NilDarkColors = darkColorScheme(
    primary = RedPrimary,
    onPrimary = WhitePrimary,
    background = BlackPrimary,
    onBackground = WhitePrimary,
    surface = BlackSurface,
    onSurface = WhitePrimary
)

@Composable
fun NILTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) NilDarkColors else NilLightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
