package com.example.tysyacha_calc.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.PlayerRoundResult
import com.example.tysyacha_calc.domain.RospisEngine
import com.example.tysyacha_calc.ui.components.DeltaText
import com.example.tysyacha_calc.ui.components.SectionCard
import com.example.tysyacha_calc.ui.components.describe
import com.example.tysyacha_calc.ui.components.formatScore
import com.example.tysyacha_calc.ui.components.isNotable
import com.example.tysyacha_calc.viewmodel.CalculatorUiState
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

/** Итог кона со всеми правилами — что будет записано по кнопке. */
@Composable
fun RoundPreviewSection(state: CalculatorUiState, modifier: Modifier = Modifier) {
    val game = state.game ?: return
    SectionCard(
        title = "Итог кона",
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        val preview = state.preview
        when {
            preview != null -> preview.forEachIndexed { i, result ->
                if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PreviewRow(game, result)
            }
            else -> state.errors.forEach { Hint(it, isError = true) }
        }
        state.warnings.forEach { Hint(it, isWarning = true) }
    }
}

@Composable
private fun PreviewRow(game: GameState, result: PlayerRoundResult) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(game.player(result.playerId).name, style = MaterialTheme.typography.titleSmall)
            result.events.forEach { event ->
                Text(
                    event.describe(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (event.isNotable) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            DeltaText(result.delta, style = MaterialTheme.typography.titleLarge)
            Text(
                "→ ${formatScore(result.scoreAfter)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Hint(text: String, isError: Boolean = false, isWarning: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = when {
            isError -> MaterialTheme.colorScheme.error
            isWarning -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

/** Карточка «роспись» вместо ввода рук. */
@Composable
fun RospisCard(game: GameState, takerId: Int, bid: Int, modifier: Modifier = Modifier) {
    val split = RospisEngine.splitAmount(bid)
    val others = RospisEngine.recipients(game.activePlayerIds, takerId).map { game.player(it).name }
    SectionCard(
        title = "Роспись на $bid",
        subtitle = "Кон не разыгрывается",
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            "${others.joinToString(" и ")} получают по $split.\n${game.player(takerId).name} ничего не теряет.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@ThemePreviews
@Composable
private fun RoundPreviewPreview() {
    PreviewBox { RoundPreviewSection(PreviewData.calculatorFilled) }
}

@ThemePreviews
@Composable
private fun RospisCardPreview() {
    PreviewBox { RospisCard(PreviewData.game3, takerId = 2, bid = 125) }
}
