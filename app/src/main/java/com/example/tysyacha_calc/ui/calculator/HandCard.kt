package com.example.tysyacha_calc.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.domain.CardPickValidator
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.ui.components.DeltaText
import com.example.tysyacha_calc.ui.components.Tag
import com.example.tysyacha_calc.viewmodel.HandUi
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

/** Взявший прикуп: очки — остаток от 120, вводятся только марьяжи и «длинная». */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TakerCard(
    hand: HandUi,
    settings: GameSettings,
    bid: Int,
    isLong: Boolean,
    onToggleMarriage: (Suit) -> Unit,
    onToggleAce: () -> Unit,
    onToggleLong: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    PlayerSurface(modifier, highlighted = true) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Name(hand.name, Modifier.weight(1f, fill = false))
            Tag("прикуп · $bid", container = colors.secondary, content = colors.onSecondary)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Column {
                Text("Картами", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                Text("${hand.cardPoints}", style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (isLong) "длинная — все карты" else "${GameRules.CARD_POINTS_TOTAL} − остальные",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            RoundScore(hand)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MarriageButtons(hand, settings, onToggleMarriage, onToggleAce)
            LongButton(selected = isLong, onClick = onToggleLong)
        }
    }
}

/** Не бравший прикуп: очки числом или кнопками-картами, марьяжи. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DefenderCard(
    hand: HandUi,
    settings: GameSettings,
    onPointsChange: (String) -> Unit,
    onAddCard: (Rank) -> Unit,
    onRemoveCard: (Rank) -> Unit,
    onToggleMarriage: (Suit) -> Unit,
    onToggleAce: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var showCards by rememberSaveable(hand.playerId) { mutableStateOf(false) }

    PlayerSurface(modifier.alpha(if (hand.isLocked) 0.6f else 1f), highlighted = false) {
        Name(hand.name)
        if (hand.isLocked) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Длинная — взяток нет", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                RoundScore(hand)
            }
            return@PlayerSurface
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val fromCards = hand.draft.cards.isNotEmpty()
            OutlinedTextField(
                value = if (fromCards) "${CardPickValidator.pointsOf(hand.draft.cards)}" else hand.draft.pointsText,
                onValueChange = onPointsChange,
                label = { Text("Картами") },
                placeholder = { Text("0") },
                supportingText = {
                    Text(
                        "макс. ${hand.maxPoints}"
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.width(140.dp),
            )
            Spacer(Modifier.weight(1f))
            RoundScore(hand)
        }

        Column {
            TextButton(onClick = { showCards = !showCards }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(if (fromCardsLabel(hand)) "Карты: ${cardsSummary(hand.draft.cards)}" else "Набрать картами")
                Icon(
                    if (showCards) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            AnimatedVisibility(showCards) {
                CardRow(
                    cards = hand.draft.cards,
                    remaining = { hand.remaining[it] ?: 0 },
                    onAdd = onAddCard,
                    onRemove = onRemoveCard,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MarriageButtons(hand, settings, onToggleMarriage, onToggleAce)
        }
    }
}

private fun fromCardsLabel(hand: HandUi) = hand.draft.cards.isNotEmpty()

private fun cardsSummary(cards: Map<Rank, Int>): String =
    Rank.entries.filter { (cards[it] ?: 0) > 0 }
        .joinToString(" ") { r -> cards[r]!!.let { if (it > 1) "${r.label}×$it" else r.label } }

@Composable
private fun MarriageButtons(
    hand: HandUi,
    settings: GameSettings,
    onToggleMarriage: (Suit) -> Unit,
    onToggleAce: () -> Unit,
) {
    Suit.entries.forEach { suit ->
        MarriageButton(
            suit = suit,
            value = settings.marriageValue(suit),
            selected = suit in hand.draft.marriages,
            enabled = suit !in hand.blockedSuits,
            onClick = { onToggleMarriage(suit) },
        )
    }
    AceMarriageButton(
        value = settings.aceMarriageValue,
        selected = hand.draft.hasAceMarriage,
        enabled = !hand.aceBlocked,
        onClick = onToggleAce,
    )
}

@Composable
private fun PlayerSurface(
    modifier: Modifier,
    highlighted: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (highlighted) colors.surfaceContainer else colors.surfaceContainerLow,
        border = if (highlighted) BorderStroke(1.5.dp, colors.secondary) else null,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
    }
}

@Composable
private fun Name(name: String, modifier: Modifier = Modifier) {
    Text(
        name,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Справа: «набрал 87» и очки в таблицу за кон. */
@Composable
private fun RoundScore(hand: HandUi) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.End) {
        Text(
            if (hand.marriagePoints > 0) "${hand.cardPoints} + ${hand.marriagePoints} = ${hand.total}"
            else "итого ${hand.total}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        DeltaText(hand.roundScore, style = MaterialTheme.typography.headlineMedium)
        when {
            hand.isTaker -> Text(
                if (hand.roundScore > 0) "ставка сыграна" else "не сыграна",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            hand.roundScore == 0 -> Text("болт", style = MaterialTheme.typography.labelMedium, color = colors.error)
        }
    }
}

@ThemePreviews
@Composable
private fun TakerCardPreview() {
    val state = PreviewData.calculatorFilled
    PreviewBox {
        TakerCard(state.hands.first { it.isTaker }, state.game!!.settings, bid = 130, isLong = false, {}, {}, {})
    }
}

@ThemePreviews
@Composable
private fun DefenderCardPreview() {
    val state = PreviewData.calculatorFilled
    PreviewBox {
        DefenderCard(state.hands.first { !it.isTaker }, state.game!!.settings, {}, {}, {}, {}, {})
    }
}
