package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/**
 * Масть и стоимость марьяжа по умолчанию (в партии берётся из [GameSettings]).
 * U+FE0E в символе — просьба рисовать масть текстом, а не цветным эмодзи.
 */
@Serializable
enum class Suit(
    val symbol: String,
    val title: String,
    val isRed: Boolean,
    val defaultMarriage: Int,
) {
    SPADES("♠\uFE0E", "Пики", isRed = false, defaultMarriage = 40),
    CLUBS("♣\uFE0E", "Трефы", isRed = false, defaultMarriage = 60),
    DIAMONDS("♦\uFE0E", "Бубны", isRed = true, defaultMarriage = 80),
    HEARTS("♥\uFE0E", "Черви", isRed = true, defaultMarriage = 100),
}
