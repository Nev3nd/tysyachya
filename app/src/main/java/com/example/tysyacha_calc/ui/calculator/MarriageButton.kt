package com.example.tysyacha_calc.ui.calculator

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.unit.dp

/** Кнопка объявления марьяжа масти: «♥ 100». */
@Composable
fun MarriageButton(
    suit: Suit,
    value: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val suitColor = if (suit.isRed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
    MarriageChip(
        label = buildAnnotatedString {
            withStyle(SpanStyle(color = suitColor, fontWeight = FontWeight.Bold)) {
                append(suit.symbol)
            }
            append(" $value")
        },
        description = "Марьяж ${suit.title}, $value",
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    )
}

/** Кнопка тузового марьяжа (все четыре туза). */
@Composable
fun AceMarriageButton(
    value: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MarriageChip(
        label = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("A×4") }
            append(" $value")
        },
        description = "Тузовый марьяж, $value",
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun MarriageChip(
    label: AnnotatedString,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val haptics = LocalHapticFeedback.current
    FilterChip(
        selected = selected,
        onClick = { haptics.performHapticFeedback(HapticFeedbackType.ToggleOn); onClick() },
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(),
        modifier = modifier,
    )
}


/** «Длинная»: взявший прикуп забрал все карты — ему 120 картами, остальным ничего. */
@Composable
fun LongButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MarriageChip(
        label = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Длинная") }
        },
        description = "Длинная — все карты у взявшего прикуп",
        selected = selected,
        enabled = true,
        onClick = onClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@ThemePreviews
@Composable
private fun MarriageButtonsPreview() {
    PreviewBox {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MarriageButton(Suit.SPADES, 40, selected = false, enabled = true, onClick = {})
            MarriageButton(Suit.CLUBS, 60, selected = false, enabled = false, onClick = {})
            MarriageButton(Suit.DIAMONDS, 80, selected = false, enabled = true, onClick = {})
            MarriageButton(Suit.HEARTS, 100, selected = true, enabled = true, onClick = {})
            AceMarriageButton(200, selected = false, enabled = true, onClick = {})
            LongButton(selected = true, onClick = {})
        }
    }
}
