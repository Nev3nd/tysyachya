package com.example.tysyacha_calc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = FeltGreen,
    onPrimary = Color.White,
    primaryContainer = FeltGreenContainer,
    onPrimaryContainer = OnFeltGreenContainer,
    secondary = Brass,
    onSecondary = Color.White,
    secondaryContainer = BrassContainer,
    onSecondaryContainer = OnBrassContainer,
    tertiary = CardRed,
    onTertiary = Color.White,
    tertiaryContainer = CardRedContainer,
    onTertiaryContainer = OnCardRedContainer,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperHighest,
    onSurfaceVariant = InkVariant,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = PaperLow,
    surfaceContainer = PaperMid,
    surfaceContainerHigh = PaperHigh,
    surfaceContainerHighest = PaperHighest,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
)

private val DarkColors = darkColorScheme(
    primary = FeltGreenDark,
    onPrimary = OnFeltGreenDark,
    primaryContainer = FeltGreenContainerDark,
    onPrimaryContainer = OnFeltGreenContainerDark,
    secondary = BrassDark,
    onSecondary = OnBrassDark,
    secondaryContainer = BrassContainerDark,
    onSecondaryContainer = OnBrassContainerDark,
    tertiary = CardRedDark,
    onTertiary = OnCardRedDark,
    tertiaryContainer = CardRedContainerDark,
    onTertiaryContainer = OnCardRedContainerDark,
    background = Night,
    onBackground = Mist,
    surface = Night,
    onSurface = Mist,
    surfaceVariant = NightHighest,
    onSurfaceVariant = MistVariant,
    surfaceContainerLowest = NightLowest,
    surfaceContainerLow = NightLow,
    surfaceContainer = NightMid,
    surfaceContainerHigh = NightHigh,
    surfaceContainerHighest = NightHighest,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
)

/** Цвета, которых нет в Material: плюс/минус и «бумажные» карты. */
@Immutable
data class GameColors(
    val positive: Color,
    val negative: Color,
    /** Значок «Р»: одинаково яркий в светлой и тёмной теме, текст на нём тёмный. */
    val badgeGreen: Color = BadgeGreen,
    val badgeYellow: Color = BadgeYellow,
    val badgeRed: Color = BadgeRed,
    val onBadge: Color = OnBadge,
    val cardFace: Color = CardFace,
    val cardFaceDisabled: Color = CardFaceDisabled,
    val cardRed: Color = CardInkRed,
    val cardBlack: Color = CardInkBlack,
    val cardEdge: Color = CardEdge,
)

private val LightGameColors = GameColors(positive = PositiveLight, negative = NegativeLight)
private val DarkGameColors = GameColors(positive = PositiveDark, negative = NegativeDark)

private val LocalGameColors = staticCompositionLocalOf { LightGameColors }

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

object GameTheme {
    val colors: GameColors
        @Composable @ReadOnlyComposable get() = LocalGameColors.current
}

@Composable
fun TysyachacalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalGameColors provides if (darkTheme) DarkGameColors else LightGameColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
