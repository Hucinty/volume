package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = PastelLilac,
  onPrimary = Color(0xFF1F1A30),
  primaryContainer = Color(0xFF383152),
  onPrimaryContainer = PastelLilac,
  secondary = PastelSage,
  onSecondary = Color(0xFF10281E),
  tertiary = PastelPeach,
  background = PastelDarkBg,
  surface = PastelDarkSurface,
  surfaceContainer = PastelDarkSurfaceContainer,
  onBackground = PastelDarkText,
  onSurface = PastelDarkText,
  onSurfaceVariant = PastelDarkTextMuted
)

private val LightColorScheme = lightColorScheme(
  primary = PastelLavenderDeep,
  onPrimary = Color.White,
  primaryContainer = PastelLavenderLight,
  onPrimaryContainer = PastelLavenderDeep,
  secondary = PastelSageDeep,
  onSecondary = Color.White,
  tertiary = PastelPeachDeep,
  background = PastelCreamBg,
  surface = PastelSurfaceLight,
  surfaceContainer = PastelSurfaceContainer,
  onBackground = PastelTextDark,
  onSurface = PastelTextDark,
  onSurfaceVariant = PastelTextMuted
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our handcrafted minimal pastel theme by default
  content: @Composable () -> Unit,
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
