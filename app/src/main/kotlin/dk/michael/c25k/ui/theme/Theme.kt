package dk.michael.c25k.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object C25KPalette {
    val FjordLight = Color(0xFF5F7F8C)
    val Fjord = Color(0xFF335D70)
    val FjordDeep = Color(0xFF172E44)
    val Background = Color(0xFFF6F2EA)
    val Surface = Color(0xFFFFFCF7)
    val SurfaceTint = Color(0xFFEAEFE9)
    val Accent = Color(0xFFEAB56A)
    val AccentText = Color(0xFF3D2607)
    val TextPrimary = Color(0xFF24333A)
    val TextSecondary = Color(0xFF64727A)
    val TextMuted = Color(0xFF8A969B)
    val Run = Color(0xFF3B8A6A)
    val Walk = Color(0xFF627EA9)
}

private val LightScheme = lightColorScheme(
    primary = C25KPalette.Fjord,
    secondary = C25KPalette.Accent,
    background = C25KPalette.Background,
    surface = C25KPalette.Surface
)
private val DarkScheme = darkColorScheme(primary = C25KPalette.FjordLight, secondary = C25KPalette.Accent)

@Composable
fun C25KTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkScheme else LightScheme
    MaterialTheme(colorScheme = colors, content = content)
}

val CompletedGreen = Color(0xFFDDEBC7)
val CompletedGreenText = Color(0xFF284111)
val CancelledRed = Color(0xFFF1C0B2)
val CancelledRedText = Color(0xFF5D2017)
