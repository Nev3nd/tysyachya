package com.example.tysyacha_calc.data.model

/**
 * Партия с историей: все сохранённые коны и текущая позиция в них.
 * [state] — состояние после первых [position] конов (то, с чем сейчас работаем),
 * [full] — состояние после всех сохранённых конов (для истории).
 */
data class GameTimeline(
    val state: GameState,
    val full: GameState,
    val position: Int,
) {
    val rounds: List<GameRound> get() = full.rounds
    val totalRounds: Int get() = rounds.size
    /** Вернулись назад — впереди есть сохранённые коны. */
    val hasFuture: Boolean get() = position < totalRounds
    val canGoBack: Boolean get() = position > 0
    /** Сохранённый кон, который сейчас «вводится заново» (если вернулись назад). */
    val storedNext: GameRound? get() = rounds.getOrNull(position)
}
