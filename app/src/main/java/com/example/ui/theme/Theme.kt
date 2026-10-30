package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = Color(0xFF001B2E),
  primaryContainer = Color(0xFF003852),
  onPrimaryContainer = Color(0xFFBCE9FF),
  secondary = ConductorGold,
  onSecondary = Color(0xFF2E1C00),
  secondaryContainer = Color(0xFF4A3400),
  onSecondaryContainer = Color(0xFFFFE6A3),
  tertiary = LogicHighGreen,
  onTertiary = Color(0xFF002E24),
  background = SiliconDark,
  onBackground = TextPrimary,
  surface = SiliconSurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = SiliconCardDark,
  onSurfaceVariant = TextSecondary,
  outline = SiliconBorder,
  outlineVariant = Color(0xFF1E2D4A),
  error = SiliconErrorRed,
  onError = Color.White
)

private val LightColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = Color(0xFF001B2E),
  primaryContainer = Color(0xFF003852),
  onPrimaryContainer = Color(0xFFBCE9FF),
  secondary = ConductorGold,
  onSecondary = Color(0xFF2E1C00),
  secondaryContainer = Color(0xFF4A3400),
  onSecondaryContainer = Color(0xFFFFE6A3),
  tertiary = LogicHighGreen,
  onTertiary = Color(0xFF002E24),
  background = SiliconDark,
  onBackground = TextPrimary,
  surface = SiliconSurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = SiliconCardDark,
  onSurfaceVariant = TextSecondary,
  outline = SiliconBorder,
  outlineVariant = Color(0xFF1E2D4A),
  error = SiliconErrorRed,
  onError = Color.White
)

@Composable
fun SiliconForgeTheme(
  content: @Composable () -> Unit,
) {
  // Always provide the deep immersive dark semiconductor laboratory aesthetic
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
