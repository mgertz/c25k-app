package dk.michael.c25k.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF1D9E75)
private val LightScheme = lightColorScheme(primary = Green)
private val DarkScheme = darkColorScheme(primary = Green)

@Composable
fun C25KTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkScheme else LightScheme
    MaterialTheme(colorScheme = colors, content = content)
}

val CompletedGreen = Color(0xFFC0DD97)
val CompletedGreenText = Color(0xFF173404)
val CancelledRed = Color(0xFFF09595)
val CancelledRedText = Color(0xFF501313)
