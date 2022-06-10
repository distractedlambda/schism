package org.schism.schismatic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun SchismaticMaterialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}

private val LightColors = lightColors(
    primary = Color(0xff263238),
    primaryVariant = Color(0xff4f5b62),
    secondary = Color(0xffeceff1),
    secondaryVariant = Color(0xffffffff),
    onPrimary = Color(0xffffffff),
    onSecondary = Color(0xff000000),
)

private val DarkColors = darkColors(
    primary = Color(0xff263238),
    primaryVariant = Color(0xff000a12),
    secondary = Color(0xffeceff1),
    secondaryVariant = Color(0xffbabdbe),
    onPrimary = Color(0xffffffff),
    onSecondary = Color(0xff000000),
)
