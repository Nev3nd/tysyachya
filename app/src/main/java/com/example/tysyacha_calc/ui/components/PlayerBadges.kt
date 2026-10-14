package com.example.tysyacha_calc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.ui.theme.GameTheme
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData
import androidx.compose.foundation.layout.Column

/** Болты: три чёрточки, набранные закрашены красным. */
@Composable
fun BoltPips(count: Int, modifier: Modifier = Modifier) {
    Pips(
        filled = count,
        total = GameRules.DASH_LIMIT,
        color = GameTheme.colors.negative,
        modifier = modifier.semantics { contentDescription = "Болтов: $count из ${GameRules.DASH_LIMIT}" },
    )
}

/** Оставшиеся росписи игрока: «Р3», «Р2» зелёным, «Р1» жёлтым, «Р0» красным. */
@Composable
fun RospisBadge(left: Int, modifier: Modifier = Modifier) {
    val colors = GameTheme.colors
    val background = when {
        left >= 2 -> colors.badgeGreen
        left == 1 -> colors.badgeYellow
        else -> colors.badgeRed
    }
    Box(
        modifier
            .background(background, CircleShape)
            .padding(horizontal = 5.dp, vertical = 1.dp)
            .semantics { contentDescription = "Росписей осталось: $left" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Р$left",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.onBadge,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun Pips(filled: Int, total: Int, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .size(width = 10.dp, height = 5.dp)
                    .background(
                        if (i < filled) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(2.dp),
                    )
            )
        }
    }
}

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Surface(modifier, shape = CircleShape, color = container, contentColor = content) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            maxLines = 1,
        )
    }
}

@Composable
fun BochkaTag(attemptsLeft: Int, modifier: Modifier = Modifier) {
    Tag(
        text = "бочка · $attemptsLeft",
        modifier = modifier,
        container = MaterialTheme.colorScheme.secondary,
        content = MaterialTheme.colorScheme.onSecondary,
    )
}

@Composable
fun DealerChip(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(18.dp)
            .background(MaterialTheme.colorScheme.secondary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text("Р", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondary)
    }
}

@ThemePreviews
@Composable
private fun BadgesPreview() {
    PreviewBox {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BoltPips(0); BoltPips(1); BoltPips(2)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RospisBadge(3); RospisBadge(2); RospisBadge(1); RospisBadge(0)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BochkaTag(2); Tag("прикуп · 120"); DealerChip()
            }
        }
    }
}
