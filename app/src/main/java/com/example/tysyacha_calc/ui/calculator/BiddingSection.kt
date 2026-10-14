package com.example.tysyacha_calc.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.domain.RospisEngine
import com.example.tysyacha_calc.domain.RoundScoreCalculator
import com.example.tysyacha_calc.ui.components.SectionCard
import com.example.tysyacha_calc.ui.components.Stepper
import com.example.tysyacha_calc.viewmodel.RoundDraft
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

/** Быстрые ставки; дальше — крутить ±5. */
private val QUICK_BIDS = listOf(120, 200, 300)

/** Торги: кто взял прикуп, ставка, роспись. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BiddingSection(
    game: GameState,
    draft: RoundDraft,
    onTakerChange: (Int) -> Unit,
    onBidStep: (Int) -> Unit,
    onBidSet: (Int) -> Unit,
    onToggleRospis: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val maxBid = RoundScoreCalculator.maxBid(game.settings)
    SectionCard(
        title = "Торги",
        subtitle = "${game.player(game.firstBidderId).name} начинает со ${GameRules.MIN_BID}, дальше +${GameRules.BID_STEP}",
        modifier = modifier,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Ставка", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Stepper(
                value = "${draft.bid}",
                onDecrement = { onBidStep(-GameRules.BID_STEP) },
                onIncrement = { onBidStep(GameRules.BID_STEP) },
                canDecrement = draft.bid > GameRules.MIN_BID,
                canIncrement = draft.bid < maxBid,
                valueStyle = MaterialTheme.typography.headlineMedium,
                valueWidth = 88.dp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QUICK_BIDS.filter { it <= maxBid }.forEach { value ->
                AssistChip(
                    onClick = { haptics.performHapticFeedback(HapticFeedbackType.SegmentTick); onBidSet(value) },
                    label = { Text("$value") },
                )
            }
        }

        Text(
            if (draft.takerId == null) "Кто взял прикуп?" else "Прикуп",
            style = MaterialTheme.typography.labelLarge,
            color = if (draft.takerId == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoundDraft.turnOrder(game).forEach { id ->
                val selected = id == draft.takerId
                FilterChip(
                    selected = selected,
                    onClick = { haptics.performHapticFeedback(HapticFeedbackType.ToggleOn); onTakerChange(id) },
                    label = { Text(game.player(id).name) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Filled.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null,
                )
            }
        }

        if (draft.takerId != null) {
            val left = game.rospisLeftFor(draft.takerId)
            val canRospis = left > 0 || draft.isRospis
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Расписаться", style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (canRospis) {
                            "Остальным по ${RospisEngine.splitAmount(draft.bid)} · осталось $left из ${GameRules.ROSPIS_LIMIT}"
                        } else "Росписей не осталось",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(checked = draft.isRospis, onCheckedChange = { onToggleRospis() }, enabled = canRospis)
            }
        }
    }
}

@ThemePreviews
@Composable
private fun BiddingNoTakerPreview() {
    val state = PreviewData.calculatorNoTaker
    PreviewBox { BiddingSection(state.game!!, state.draft!!, {}, {}, {}, {}) }
}

@ThemePreviews
@Composable
private fun BiddingWithTakerPreview() {
    val state = PreviewData.calculatorFilled
    PreviewBox { BiddingSection(state.game!!, state.draft!!, {}, {}, {}, {}) }
}
