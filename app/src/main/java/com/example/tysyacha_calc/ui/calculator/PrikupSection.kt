package com.example.tysyacha_calc.ui.calculator

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.domain.CardPickValidator
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.domain.PrikupHandler
import com.example.tysyacha_calc.ui.components.DeltaText
import com.example.tysyacha_calc.ui.components.SectionCard
import com.example.tysyacha_calc.viewmodel.PrikupUi
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData

/** Игра вчетвером: раздающий записывает себе очки трёх карт прикупа. */
@Composable
fun PrikupSection(
    prikup: PrikupUi,
    onPointsChange: (String) -> Unit,
    onAddCard: (Rank) -> Unit,
    onRemoveCard: (Rank) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = "Прикуп раздающему",
        subtitle = "${prikup.name} не играет — записывает очки ${GameRules.PRIKUP_CARDS} карт прикупа",
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = if (prikup.cards.isNotEmpty()) "${prikup.points}" else prikup.text,
                onValueChange = onPointsChange,
                label = { Text("Очки прикупа") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.width(150.dp),
            )
            Spacer(Modifier.weight(1f))
            DeltaText(prikup.roundScore, style = MaterialTheme.typography.headlineMedium)
        }
        CardRow(
            cards = prikup.cards,
            remaining = { rank ->
                if (PrikupHandler.canAddCard(prikup.cards, rank)) Rank.COPIES - (prikup.cards[rank] ?: 0)
                else 0
            },
            onAdd = onAddCard,
            onRemove = onRemoveCard,
        )
        if (prikup.cards.isNotEmpty()) {
            Text(
                "Выбрано ${CardPickValidator.countOf(prikup.cards)} из ${GameRules.PRIKUP_CARDS}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun PrikupPreview() {
    PreviewBox { PrikupSection(PreviewData.calculator4.prikup!!, {}, {}, {}) }
}
