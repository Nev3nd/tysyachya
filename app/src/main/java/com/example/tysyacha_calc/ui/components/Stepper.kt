package com.example.tysyacha_calc.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

@Composable
fun Stepper(
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    canDecrement: Boolean = true,
    canIncrement: Boolean = true,
    valueStyle: TextStyle = MaterialTheme.typography.titleLarge,
    valueWidth: Dp = 56.dp,
) {
    val haptics = LocalHapticFeedback.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(
            onClick = { haptics.performHapticFeedback(HapticFeedbackType.SegmentTick); onDecrement() },
            enabled = canDecrement,
        ) { Text("−", style = MaterialTheme.typography.titleLarge) }
        Text(
            value,
            style = valueStyle,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = valueWidth),
        )
        FilledTonalIconButton(
            onClick = { haptics.performHapticFeedback(HapticFeedbackType.SegmentTick); onIncrement() },
            enabled = canIncrement,
        ) { Icon(Icons.Filled.Add, contentDescription = "Больше") }
    }
}

@ThemePreviews
@Composable
private fun StepperPreview() {
    PreviewBox { Stepper(value = "125", onDecrement = {}, onIncrement = {}) }
}
