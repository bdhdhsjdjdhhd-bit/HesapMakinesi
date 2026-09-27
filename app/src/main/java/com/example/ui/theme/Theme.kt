package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IosDarkColorScheme =
  darkColorScheme(
    primary = IosOrange,
    onPrimary = Color.White,
    primaryContainer = IosElevatedSurface,
    onPrimaryContainer = Color.White,
    secondary = IosBlue,
    onSecondary = Color.White,
    secondaryContainer = IosElevatedCard,
    onSecondaryContainer = Color.White,
    tertiary = IosCyan,
    background = IosPureBlack,
    onBackground = Color.White,
    surface = IosElevatedCard,
    onSurface = Color.White,
    surfaceVariant = IosElevatedSurface,
    onSurfaceVariant = IosSecondaryText,
    error = IosPink,
    onError = Color.White,
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = IosDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
