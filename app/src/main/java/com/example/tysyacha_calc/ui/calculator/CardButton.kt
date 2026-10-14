package com.example.tysyacha_calc.ui.calculator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.ui.theme.GameTheme
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData
import androidx.compose.foundation.layout.width

/**
 * Кнопка-карта: тап — добавить карту игроку, долгое нажатие или «−» — убрать.
 * Внизу четыре масти: погасшие — карты этого номинала, уже разобранные за кон.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardButton(
    rank: Rank,
    count: Int,
    /** Сколько ещё карт этого номинала осталось в колоде (для всех игроков). */
    remaining: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    totalCopies: Int = Rank.COPIES,
) {
    val haptics = LocalHapticFeedback.current
    val canAdd = remaining > 0
    val colors = GameTheme.colors
    val selected = count > 0
    val scale by animateFloatAsState(if (selected) 1f else 0.96f, label = "cardScale")
    val accent = MaterialTheme.colorScheme.primary
    val isRedRank = rank.ordinal % 2 == 1 // чередуем цвет номинала, как на разных мастях

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .scale(scale)
                .alpha(if (canAdd || selected) 1f else 0.45f)
                .shadow(if (selected) 4.dp else 1.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(if (canAdd || selected) colors.cardFace else colors.cardFaceDisabled)
                .border(
                    if (selected) BorderStroke(2.dp, accent) else BorderStroke(1.dp, colors.cardEdge),
                    RoundedCornerShape(8.dp),
                )
                .combinedClickable(
                    enabled = canAdd || selected,
                    role = Role.Button,
                    onClick = {
                        if (canAdd) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onAdd()
                        }
                    },
                    onLongClick = {
                        if (selected) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRemove()
                        }
                    },
                )
                .semantics {
                    contentDescription = "${rank.label}, ${rank.points} очков, выбрано $count, осталось $remaining"
                },
        ) {
            Text(
                rank.label,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isRedRank) colors.cardRed else colors.cardBlack,
            )
            Text(
                rank.label,
                modifier = Modifier.align(Alignment.Center).offset(y = (-4).dp),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = if (rank == Rank.TEN) 20.sp else 24.sp,
                color = if (isRedRank) colors.cardRed else colors.cardBlack,
            )
            SuitPips(
                taken = totalCopies - remaining,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp),
            )
            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = accent,
                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(18.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
        Text(
            if (selected) "−" else "${rank.points}",
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .then(
                    if (selected) Modifier.combinedClickable(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onRemove()
                    }) else Modifier
                )
                .padding(horizontal = 10.dp),
        )
    }
}

/** ♠♥♣♦: первые [taken] гаснут. */
@Composable
private fun SuitPips(taken: Int, modifier: Modifier = Modifier) {
    val colors = GameTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        listOf(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS).forEachIndexed { i, suit ->
            Text(
                suit.symbol,
                fontSize = 9.sp,
                color = (if (suit.isRed) colors.cardRed else colors.cardBlack)
                    .copy(alpha = if (i < taken) 0.18f else 1f),
            )
        }
    }
}

/** Ряд из шести кнопок-карт 9, 10, J, Q, K, A. */
@Composable
fun CardRow(
    cards: Map<Rank, Int>,
    remaining: (Rank) -> Int,
    onAdd: (Rank) -> Unit,
    onRemove: (Rank) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Rank.entries.forEach { rank ->
            CardButton(
                rank = rank,
                count = cards[rank] ?: 0,
                remaining = remaining(rank),
                onAdd = { onAdd(rank) },
                onRemove = { onRemove(rank) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CardRowPreview() {
    PreviewBox {
        CardRow(
            cards = mapOf(Rank.ACE to 2, Rank.KING to 1),
            remaining = { if (it == Rank.TEN) 0 else 2 },
            onAdd = {},
            onRemove = {},
        )
    }
}

@ThemePreviews
@Composable
private fun CardButtonPreview() {
    PreviewBox { CardButton(Rank.ACE, count = 2, remaining = 1, onAdd = {}, onRemove = {}, modifier = Modifier.width(64.dp)) }
}
