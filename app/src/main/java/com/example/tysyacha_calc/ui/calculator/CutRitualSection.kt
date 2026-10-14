package com.example.tysyacha_calc.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.domain.CutRitualEngine
import com.example.tysyacha_calc.ui.components.SectionCard
import com.example.tysyacha_calc.ui.components.Stepper
import com.example.tysyacha_calc.ui.components.formatDelta
import com.example.tysyacha_calc.viewmodel.RoundDraft
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

/** Ритуал сдвига колоды: сколько девяток и вальтов выпало снизу. */
@Composable
fun CutRitualSection(
    game: GameState,
    draft: RoundDraft,
    onNines: (Int) -> Unit,
    onJacks: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cutter = game.player(game.cutterId).name
    val dealer = game.player(game.dealerId).name
    SectionCard(
        title = "Сдвиг колоды",
        subtitle = "Сдвигает $cutter, раздаёт $dealer",
        modifier = modifier,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CutCounter("Девятки", draft.nines, cutter, onNines, Modifier.weight(1f))
            CutCounter("Вальты", draft.jacks, dealer, onJacks, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CutCounter(
    title: String,
    count: Int,
    penalized: String,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val penalty = CutRitualEngine.penaltyFor(count)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Stepper(
            value = "$count",
            onDecrement = { onChange(-1) },
            onIncrement = { onChange(1) },
            canDecrement = count > 0,
            valueWidth = 40.dp,
        )
        Text(
            if (penalty != 0) "$penalized ${formatDelta(penalty)}" else "каждые 3 — −120",
            style = MaterialTheme.typography.labelSmall,
            color = if (penalty != 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@ThemePreviews
@Composable
private fun CutRitualPreview() {
    val state = PreviewData.calculatorFilled
    PreviewBox { CutRitualSection(state.game!!, state.draft!!.copy(nines = 3, jacks = 1), {}, {}) }
}
