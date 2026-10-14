package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/** Что набрал один игравший игрок за кон. */
@Serializable
data class RoundEntry(
    val playerId: Int,
    /** Очки за взятые карты, без округления (0..120). */
    val cardPoints: Int,
    val marriages: List<Suit> = emptyList(),
    val hasAceMarriage: Boolean = false,
    /** Если очки набирались кнопками-картами — какие карты (для детализации в истории). */
    val cards: Map<Rank, Int> = emptyMap(),
    /** Очки посчитаны автоматически: 120 минус остальные. */
    val isAuto: Boolean = false,
)
