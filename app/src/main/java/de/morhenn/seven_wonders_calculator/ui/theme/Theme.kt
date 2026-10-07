package de.morhenn.seven_wonders_calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Brand colors that stay the same in light and dark theme. */
object Brand {
    val Lapis = Color(0xFF1F3F73)
    val LapisDeep = Color(0xFF0E2245)
    val Night = Color(0xFF0A1428)
    val Gold = Color(0xFFE2B33C)
    val GoldLight = Color(0xFFF6D77F)
    val GoldDeep = Color(0xFFB07F1E)
    val Parchment = Color(0xFFF8F1E1)
    val Silver = Color(0xFFC9D0D8)
    val SilverDeep = Color(0xFF8D96A1)
    val Bronze = Color(0xFFD99A62)
    val BronzeDeep = Color(0xFF9A5B2B)
    val Terracotta = Color(0xFFB4532F)
}

// Warm parchment with lapis and gold.
private val LightColors = lightColorScheme(
    primary = Color(0xFF24508F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF0B1F45),
    secondary = Color(0xFF7E5B00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3A3),
    onSecondaryContainer = Color(0xFF2A1D00),
    tertiary = Color(0xFFA0482A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF3A0B00),
    background = Color(0xFFF8F4EC),
    onBackground = Color(0xFF1E1B16),
    surface = Color(0xFFF8F4EC),
    onSurface = Color(0xFF1E1B16),
    surfaceVariant = Color(0xFFEAE2D3),
    onSurfaceVariant = Color(0xFF50483B),
    outline = Color(0xFF837A6C),
    outlineVariant = Color(0xFFD4CABA),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3EEE4),
    surfaceContainer = Color(0xFFEEE8DC),
    surfaceContainerHigh = Color(0xFFE8E1D4),
    surfaceContainerHighest = Color(0xFFE1DACC),
)

// Lapis night sky with gold.
private val DarkColors = darkColorScheme(
    primary = Color(0xFFAFC6FF),
    onPrimary = Color(0xFF0A2A5E),
    primaryContainer = Color(0xFF1D3F78),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFFF0C35A),
    onSecondary = Color(0xFF3D2B00),
    secondaryContainer = Color(0xFF5A4200),
    onSecondaryContainer = Color(0xFFFFE3A3),
    tertiary = Color(0xFFFFB59A),
    onTertiary = Color(0xFF5A1B00),
    tertiaryContainer = Color(0xFF7E2E10),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF0E131C),
    onBackground = Color(0xFFE6E1D8),
    surface = Color(0xFF0E131C),
    onSurface = Color(0xFFE6E1D8),
    surfaceVariant = Color(0xFF2A3343),
    onSurfaceVariant = Color(0xFFC3BBAE),
    outline = Color(0xFF8E8678),
    outlineVariant = Color(0xFF3A4252),
    surfaceContainerLowest = Color(0xFF0A0E15),
    surfaceContainerLow = Color(0xFF151B26),
    surfaceContainer = Color(0xFF19202C),
    surfaceContainerHigh = Color(0xFF212938),
    surfaceContainerHighest = Color(0xFF2A3343),
)

private val base = Typography()
private val Serif = FontFamily.Serif

private val AppTypography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = Serif, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = Serif, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = Serif, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = Serif, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Serif, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Serif, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Serif, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)

/** Tabular figures so columns of numbers line up. */
val NumberStyle = TextStyle(fontFeatureSettings = "tnum", fontWeight = FontWeight.SemiBold)

/** Big scores: serif numerals, like engraved stone. */
val ScoreStyle = TextStyle(fontFamily = Serif, fontFeatureSettings = "tnum, lnum", fontWeight = FontWeight.Bold)

/** Widely tracked small caps for section headers; apply to uppercase text. */
val OverlineStyle = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.2.sp)

@Composable
fun SevenWondersTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
