package com.example.tysyacha_calc.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.tysyacha_calc.ui.theme.GameTheme
import com.example.tysyacha_calc.ui.preview.ThemePreviews
import com.example.tysyacha_calc.ui.preview.PreviewBox
import com.example.tysyacha_calc.ui.preview.PreviewData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp

/** «+120» зелёным, «−60» красным, «0» нейтрально. */
@Composable
fun DeltaText(
    delta: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val colors = GameTheme.colors
    Text(
        text = formatDelta(delta),
        modifier = modifier,
        style = style,
        color = when {
            delta > 0 -> colors.positive
            delta < 0 -> colors.negative
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

fun formatDelta(delta: Int): String = when {
    delta > 0 -> "+$delta"
    delta < 0 -> "−${-delta}"
    else -> "0"
}

fun formatScore(score: Int): String = if (score < 0) "−${-score}" else "$score"

@ThemePreviews
@Composable
private fun DeltaTextPreview() {
    PreviewBox {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DeltaText(120, style = MaterialTheme.typography.headlineSmall)
            DeltaText(0, style = MaterialTheme.typography.headlineSmall)
            DeltaText(-120, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
