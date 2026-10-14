package com.example.tysyacha_calc.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.GameTimeline
import com.example.tysyacha_calc.data.model.RoundResult
import com.example.tysyacha_calc.data.model.ScoreEvent
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.ui.components.DeltaText
import com.example.tysyacha_calc.ui.components.ScoreBoard
import com.example.tysyacha_calc.ui.components.formatScore
import com.example.tysyacha_calc.ui.components.isNotable
import com.example.tysyacha_calc.viewmodel.AppViewModelProvider
import com.example.tysyacha_calc.viewmodel.HistoryUiState
import com.example.tysyacha_calc.viewmodel.HistoryViewModel
import com.example.tysyacha_calc.ui.preview.PreviewData
import com.example.tysyacha_calc.ui.preview.ScreenPreviews
import com.example.tysyacha_calc.ui.theme.TysyachacalcTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onContinue: (Long) -> Unit,
    gameId: Long,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreenContent(
        state = state,
        onBack = onBack,
        onContinue = { onContinue(gameId) },
        onReturnToRound = { number -> viewModel.returnToRound(number) { onContinue(gameId) } },
    )
}

/** Вёрстка истории без ViewModel — её видно в превью. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenContent(
    state: HistoryUiState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onReturnToRound: (Int) -> Unit,
) {
    val timeline = state.timeline
    val game = timeline?.state
    var selectedRound by rememberSaveable { mutableStateOf<Int?>(null) }
    var oldestFirst by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(onClick = { oldestFirst = !oldestFirst }) {
                        Text(if (oldestFirst) "Сначала старые" else "Сначала новые")
                    }
                    if (game != null && !game.isFinished) {
                        IconButton(onClick = onContinue) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "К вводу кона")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            timeline == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Партия не найдена")
            }
            else -> HistoryContent(
                timeline,
                PaddingValues(top = padding.calculateTopPadding()),
                oldestFirst = oldestFirst,
                onRoundClick = { selectedRound = it },
            )
        }
    }

    val round = selectedRound?.let { timeline?.full?.results?.getOrNull(it) }
    if (timeline != null && round != null) {
        RoundDetailSheet(
            game = timeline.full,
            result = round,
            isFuture = round.round.roundNumber > timeline.position,
            onReturn = {
                selectedRound = null
                onReturnToRound(round.round.roundNumber)
            },
            onDismiss = { selectedRound = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryContent(
    timeline: GameTimeline,
    padding: PaddingValues,
    oldestFirst: Boolean,
    onRoundClick: (Int) -> Unit,
) {
    val game = timeline.state
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.padding(padding)) {
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreBoard(game, showDealer = false)
                Text(
                    "Конов: ${game.rounds.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        stickyHeader { HeaderRow(game) }
        if (timeline.full.results.isEmpty()) {
            item {
                Text(
                    "Конов пока нет",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                )
            }
        }
        val results = timeline.full.results.withIndex().toList().let { if (oldestFirst) it else it.reversed() }
        itemsIndexed(results, key = { _, it -> it.index }) { _, (index, result) ->
            val isFuture = index >= timeline.position
            // Граница между применёнными конами и «будущими» (после возврата назад).
            val atBoundary = timeline.hasFuture &&
                if (oldestFirst) index == timeline.position else index == timeline.position - 1
            if (atBoundary) {
                Text(
                    if (oldestFirst) "▼ дальше — коны после возврата назад, заменятся при новой записи"
                    else "▲ впереди — коны после возврата назад, заменятся при новой записи",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
            RoundRow(game, result, onClick = { onRoundClick(index) }, modifier = Modifier.alpha(if (isFuture) 0.45f else 1f))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

private val NumberColumn = 64.dp

@Composable
private fun HeaderRow(game: GameState) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Кон", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(NumberColumn))
        game.players.forEach {
            Text(
                it.name,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RoundRow(game: GameState, result: RoundResult, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val round = result.round
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(NumberColumn)) {
            Text("${round.roundNumber}", style = MaterialTheme.typography.titleMedium)
            Text(
                when {
                    round.isRospis -> "роспись"
                    round.isLong -> "${round.bid.amount} длин."
                    else -> "${round.bid.amount}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
        game.players.forEach { player ->
            val r = result.forPlayer(player.id)
            val isTaker = player.id == round.bid.takerId
            val isPrikup = player.id == round.prikup?.playerId
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .background(
                        when {
                            isTaker -> colors.secondaryContainer.copy(alpha = 0.55f)
                            isPrikup -> colors.surfaceContainerHighest.copy(alpha = 0.6f)
                            else -> colors.surface.copy(alpha = 0f)
                        },
                        MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (r.events.any { it.isNotable && it !is ScoreEvent.Dash }) {
                        Box(Modifier.size(6.dp).background(colors.tertiary, CircleShape))
                    }
                    if (r.isDash && r.delta == 0) {
                        Text("—", style = MaterialTheme.typography.titleMedium, color = colors.tertiary)
                    } else {
                        DeltaText(r.delta, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Text(
                    formatScore(r.scoreAfter),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}


@ScreenPreviews
@Composable
private fun HistoryPreview() {
    TysyachacalcTheme {
        HistoryScreenContent(HistoryUiState(isLoading = false, timeline = PreviewData.timeline(PreviewData.game3)), {}, {}, {})
    }
}

@ScreenPreviews
@Composable
private fun HistoryAfterStepBackPreview() {
    TysyachacalcTheme {
        HistoryScreenContent(
            HistoryUiState(isLoading = false, timeline = PreviewData.timeline(PreviewData.game3, position = 1)),
            {}, {}, {},
        )
    }
}
