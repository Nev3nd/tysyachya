package com.example.tysyacha_calc.ui.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.GameTimeline
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

/** Плашка «вы вернулись назад»: можно шагнуть вперёд без изменений или переписать кон. */
@Composable
fun TimelineBanner(
    timeline: GameTimeline,
    onForward: () -> Unit,
    onLatest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = timeline.state.nextRoundNumber
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Кон $current из ${timeline.totalRounds}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                "Исправь и запиши — дальнейшие коны заменятся. Без правок «Записать» просто шагнёт вперёд.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onForward) { Text("Вперёд") }
                FilledTonalButton(onClick = onLatest) { Text("К последнему") }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun TimelineBannerPreview() {
    PreviewBox { TimelineBanner(PreviewData.calculatorTimeline.timeline!!, onForward = {}, onLatest = {}) }
}
