package com.example.tysyacha_calc.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.PlayerRoundResult
import com.example.tysyacha_calc.domain.Rule555
import com.example.tysyacha_calc.ui.theme.ScoreTextStyle
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

/** Табло: по плитке на игрока — счёт, болты, росписи, бочка, кто раздаёт. */
@Composable
fun ScoreBoard(
    game: GameState,
    modifier: Modifier = Modifier,
    preview: List<PlayerRoundResult>? = null,
    showDealer: Boolean = true,
) {
    Row(
        modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        game.players.forEach { player ->
            val state = game.stateOf(player.id)
            PlayerTile(
                name = player.name,
                score = state.score,
                dashCount = state.dashCount,
                rospisLeft = game.rospisLeftFor(player.id),
                bochkaAttemptsLeft = state.bochkaAttemptsLeft.takeIf { state.isOnBochka },
                isDealer = showDealer && !game.isFinished && player.id == game.dealerId,
                isWinner = player.id == game.winnerId,
                distanceTo555 = if (game.settings.use555Rule && game.settings.show555Progress) Rule555.distance(state.score) else null,
                preview = preview?.firstOrNull { it.playerId == player.id },
                compact = game.players.size == 4,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun PlayerTile(
    name: String,
    score: Int,
    dashCount: Int,
    rospisLeft: Int,
    bochkaAttemptsLeft: Int?,
    isDealer: Boolean,
    isWinner: Boolean,
    distanceTo555: Int?,
    preview: PlayerRoundResult?,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (isWinner) colors.secondaryContainer else colors.surfaceContainerHigh,
        border = when {
            isWinner -> BorderStroke(2.dp, colors.secondary)
            isDealer -> BorderStroke(1.dp, colors.secondary.copy(alpha = 0.6f))
            else -> null
        },
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isDealer) {
                    Spacer(Modifier.padding(start = 4.dp))
                    DealerChip()
                }
            }
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    val up = targetState > initialState
                    (slideInVertically(tween(250)) { if (up) it else -it } + fadeIn()) togetherWith
                        (slideOutVertically(tween(250)) { if (up) -it else it } + fadeOut())
                },
                label = "score",
            ) { value ->
                Text(
                    formatScore(value),
                    style = if (compact) ScoreTextStyle.copy(fontSize = 22.sp, lineHeight = 28.sp) else ScoreTextStyle,
                    color = if (value < 0) colors.tertiary else colors.onSurface,
                    maxLines = 1,
                )
            }
            if (preview != null && preview.delta != 0) {
                Text(
                    "→ ${formatScore(preview.scoreAfter)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BoltPips(dashCount)
                RospisBadge(rospisLeft)
            }
            if (bochkaAttemptsLeft != null) {
                Spacer(Modifier.height(6.dp))
                BochkaTag(bochkaAttemptsLeft)
            } else if (distanceTo555 != null && distanceTo555 <= PROGRESS_555_HINT) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "до 555: $distanceTo555",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.tertiary,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Подсказку «до 555» показываем, когда до порога осталось не больше этого. */
private const val PROGRESS_555_HINT = 150

@ThemePreviews
@Composable
private fun ScoreBoardPreview() {
    PreviewBox { ScoreBoard(PreviewData.calculatorFilled.game!!, preview = PreviewData.calculatorFilled.preview) }
}

@ThemePreviews
@Composable
private fun ScoreBoardFourPlayersPreview() {
    PreviewBox { ScoreBoard(PreviewData.game4) }
}

@ThemePreviews
@Composable
private fun ScoreBoardWinnerPreview() {
    PreviewBox { ScoreBoard(PreviewData.finished) }
}
