package com.example.tysyacha_calc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

/** Цифры одинаковой ширины — колонки очков не «пляшут». */
private const val TABULAR = "tnum"

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFeatureSettings = TABULAR, fontWeight = FontWeight.SemiBold),
    headlineLarge = base.headlineLarge.copy(fontFeatureSettings = TABULAR, fontWeight = FontWeight.SemiBold),
    headlineMedium = base.headlineMedium.copy(fontFeatureSettings = TABULAR, fontWeight = FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.copy(fontFeatureSettings = TABULAR, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFeatureSettings = TABULAR, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFeatureSettings = TABULAR),
    bodyLarge = base.bodyLarge.copy(fontFeatureSettings = TABULAR),
    bodyMedium = base.bodyMedium.copy(fontFeatureSettings = TABULAR),
    bodySmall = base.bodySmall.copy(fontFeatureSettings = TABULAR),
    labelLarge = base.labelLarge.copy(fontFeatureSettings = TABULAR),
    labelMedium = base.labelMedium.copy(fontFeatureSettings = TABULAR),
    labelSmall = base.labelSmall.copy(fontFeatureSettings = TABULAR),
)

val ScoreTextStyle = TextStyle(
    fontFeatureSettings = TABULAR,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 32.sp,
)
