package com.example.tysyacha_calc.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tysyacha_calc.data.model.GameSummary
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.ui.components.formatScore
import com.example.tysyacha_calc.viewmodel.AppViewModelProvider
import com.example.tysyacha_calc.viewmodel.HomeUiState
import com.example.tysyacha_calc.viewmodel.HomeViewModel
import com.example.tysyacha_calc.ui.preview.PreviewData
import com.example.tysyacha_calc.ui.preview.ScreenPreviews
import com.example.tysyacha_calc.ui.theme.TysyachacalcTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNewGame: () -> Unit,
    onOpenGame: (GameSummary) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state, onNewGame, onOpenGame, onDelete = viewModel::deleteGame)
}

/** Вёрстка главной без ViewModel — её видно в превью. */
@Composable
fun HomeContent(
    state: HomeUiState,
    onNewGame: () -> Unit,
    onOpenGame: (GameSummary) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var toDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.safeDrawingPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Header() }
            state.current?.let { current ->
                item {
                    CurrentGameCard(current, onContinue = { onOpenGame(current) }, onDelete = { toDelete = current.id })
                }
            }
            item {
                if (state.current == null) {
                    Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 16.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Новая партия", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    OutlinedButton(onClick = onNewGame, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 14.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Новая партия")
                    }
                }
            }
            if (state.games.isNotEmpty()) {
                item {
                    Text(
                        "Прошлые партии",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                    )
                }
                items(state.games, key = { it.id }) { game ->
                    PastGameCard(game, onClick = { onOpenGame(game) }, onDelete = { toDelete = game.id })
                }
            } else if (!state.isLoading && state.current == null) {
                item {
                    Text(
                        "Здесь появятся сыгранные партии.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    )
                }
            }
        }
    }

    toDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Удалить партию?") },
            text = { Text("Все коны этой партии будут удалены без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { onDelete(id); toDelete = null }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun Header() {
    Column(Modifier.padding(top = 32.dp, bottom = 16.dp, start = 4.dp)) {
        Text(
            buildAnnotatedString {
                Suit.entries.forEach { suit ->
                    withStyle(SpanStyle(color = if (suit.isRed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(suit.symbol)
                    }
                    append(" ")
                }
            },
            style = MaterialTheme.typography.titleLarge,
        )
        Text("Тысяча", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text(
            "Считает очки, бочку, болты и роспись — вы просто играете",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CurrentGameCard(game: GameSummary, onContinue: () -> Unit, onDelete: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (game.roundsCount == 0) "Партия начата" else "Партия идёт · сыграно конов: ${game.roundsCount}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить партию")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                game.players.forEachIndexed { i, player ->
                    Column(Modifier.weight(1f)) {
                        Text(player.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(formatScore(game.scores.getOrElse(i) { 0 }), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 16.dp)) {
                Text("Продолжить", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun PastGameCard(game: GameSummary, onClick: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${formatDate(game.updatedAt)} · ${game.roundsCount} конов",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                game.players.forEachIndexed { i, player ->
                    val isWinner = player.id == game.winnerId
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isWinner) {
                            Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                        }
                        Text(
                            player.name,
                            style = if (isWinner) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(formatScore(game.scores.getOrElse(i) { 0 }), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(2.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Удалить партию", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMMM, HH:mm", Locale.forLanguageTag("ru")).format(Date(millis))

@ScreenPreviews
@Composable
private fun HomePreview() {
    TysyachacalcTheme { HomeContent(PreviewData.home, {}, {}, {}) }
}

@ScreenPreviews
@Composable
private fun HomeEmptyPreview() {
    TysyachacalcTheme { HomeContent(HomeUiState(isLoading = false), {}, {}, {}) }
}
