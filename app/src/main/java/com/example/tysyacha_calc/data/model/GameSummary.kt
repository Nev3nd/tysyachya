package com.example.tysyacha_calc.data.model

/** Короткая сводка партии для списка на главном экране. */
data class GameSummary(
    val id: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val players: List<Player>,
    val scores: List<Int>,
    val roundsCount: Int,
    val winnerId: Int?,
) {
    val isFinished: Boolean get() = winnerId != null
}
