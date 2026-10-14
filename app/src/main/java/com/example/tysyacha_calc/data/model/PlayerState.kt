package com.example.tysyacha_calc.data.model

/**
 * Состояние игрока в текущий момент партии.
 * Не хранится, а каждый раз пересчитывается из списка конов (см. GameEngine.replay) —
 * поэтому undo — это просто «выкинуть последний кон и пересчитать».
 */
data class PlayerState(
    val playerId: Int,
    val score: Int = 0,
    val dashCount: Int = 0,             // болты за партию
    val isOnBochka: Boolean = false,
    val bochkaAttemptsLeft: Int = 0,    // имеет смысл только когда isOnBochka = true
)
