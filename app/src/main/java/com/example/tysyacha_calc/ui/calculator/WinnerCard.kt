package com.example.tysyacha_calc.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

@Composable
fun WinnerCard(
    game: GameState,
    onHistory: () -> Unit,
    onNewGame: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val winner = game.winnerId?.let(game::player) ?: return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(56.dp),
            )
            Text(
                "Победа: ${winner.name}!",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                "${game.stateOf(winner.id).score} очков за ${game.rounds.size} конов",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) { Text("Новая партия") }
            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("История партии") }
            OutlinedButton(onClick = onUndo, modifier = Modifier.fillMaxWidth()) { Text("Назад на кон") }
        }
    }
}

@ThemePreviews
@Composable
private fun WinnerCardPreview() {
    PreviewBox { WinnerCard(PreviewData.finished, onHistory = {}, onNewGame = {}, onUndo = {}) }
}
