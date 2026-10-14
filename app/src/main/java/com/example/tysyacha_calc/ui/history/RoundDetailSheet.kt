package com.example.tysyacha_calc.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.RoundEntry
import com.example.tysyacha_calc.data.model.RoundResult
import com.example.tysyacha_calc.domain.RospisEngine
import com.example.tysyacha_calc.ui.components.DeltaText
import com.example.tysyacha_calc.ui.components.describe
import com.example.tysyacha_calc.ui.components.formatScore
import com.example.tysyacha_calc.ui.components.isNotable
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData
import com.example.tysyacha_calc.ui.preview.ThemePreviews

/** Подробности кона: торги, сдвиг, карты, марьяжи и что сработало по правилам. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundDetailSheet(
    game: GameState,
    result: RoundResult,
    isFuture: Boolean,
    onReturn: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        RoundDetailContent(game, result, isFuture, onReturn, Modifier.navigationBarsPadding())
    }
}

/** Содержимое шторки отдельно — чтобы его было видно в превью. */
@Composable
fun RoundDetailContent(
    game: GameState,
    result: RoundResult,
    isFuture: Boolean,
    onReturn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val round = result.round
    val name = { id: Int -> game.player(id).name }
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Кон ${round.roundNumber}", style = MaterialTheme.typography.headlineSmall)
        if (isFuture) {
            Text(
                "Этот кон впереди — вы вернулись назад по истории",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Info("Раздача", name(round.dealerId))
        round.cutRitualResult?.takeIf { !it.isEmpty }?.let {
            Info("Сдвиг", "девяток ${it.ninesCount}, вальтов ${it.jacksCount}")
        }
        if (round.isRospis) {
            Info(
                "Роспись",
                "${name(round.bid.takerId)} на ${round.bid.amount}, остальным по ${RospisEngine.splitAmount(round.bid.amount)}",
            )
        } else {
            Info("Ставка", "${name(round.bid.takerId)} — ${round.bid.amount}${if (round.isLong) ", длинная" else ""}")
        }
        round.prikup?.let { Info("Прикуп", "${name(it.playerId)}: ${it.cardPoints}${cardsText(it.cards)}") }

        FilledTonalButton(onClick = onReturn, modifier = Modifier.fillMaxWidth()) {
            Text("Вернуться к этому кону")
        }
        HorizontalDivider()
        result.players.forEach { r ->
            val entry = round.entries.firstOrNull { it.playerId == r.playerId }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name(r.playerId), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    DeltaText(r.delta, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "  → ${formatScore(r.scoreAfter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                entry?.let { EntryLine(it, game.settings) }
                r.events.forEach { event ->
                    Text(
                        event.describe(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (event.isNotable) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryLine(entry: RoundEntry, settings: GameSettings) {
    val parts = buildList {
        add("картами ${entry.cardPoints}${if (entry.isAuto) " (по остатку)" else ""}${cardsText(entry.cards)}")
        entry.marriages.forEach { add("марьяж ${it.symbol} ${settings.marriageValue(it)}") }
        if (entry.hasAceMarriage) add("тузовый ${settings.aceMarriageValue}")
    }
    Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
}

private fun cardsText(cards: Map<Rank, Int>): String =
    if (cards.isEmpty()) ""
    else Rank.entries.filter { (cards[it] ?: 0) > 0 }
        .joinToString(" ", prefix = " (", postfix = ")") { r -> cards[r]!!.let { if (it > 1) "${r.label}×$it" else r.label } }

@Composable
private fun Info(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.65f))
    }
}

@ThemePreviews
@Composable
private fun RoundDetailPreview() {
    PreviewBox { RoundDetailContent(PreviewData.game3, PreviewData.game3.results[0], isFuture = false, onReturn = {}) }
}

@ThemePreviews
@Composable
private fun RoundDetailLongPreview() {
    PreviewBox { RoundDetailContent(PreviewData.finished, PreviewData.finished.results.last(), isFuture = true, onReturn = {}) }
}
