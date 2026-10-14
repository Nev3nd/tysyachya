package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/**
 * Настройки партии. Снимок сохраняется вместе с партией, поэтому смена настроек
 * по умолчанию не меняет счёт уже идущих игр.
 * Бочка, болты и роспись — всегда включены (см. [com.example.tysyacha_calc.domain.GameRules]).
 */
@Serializable
data class GameSettings(
    val playerCount: Int = 3,
    val allowNegativeScore: Boolean = true,
    val cutRitualEnabled: Boolean = true,
    val use555Rule: Boolean = true,
    val show555Progress: Boolean = true,
    val marriageValues: Map<Suit, Int> = DEFAULT_MARRIAGES,
    val aceMarriageValue: Int = DEFAULT_ACE_MARRIAGE,
) {
    init {
        require(playerCount == 3 || playerCount == 4) { "Игроков может быть 3 или 4, а не $playerCount" }
    }

    fun marriageValue(suit: Suit): Int = marriageValues[suit] ?: suit.defaultMarriage

    companion object {
        val DEFAULT_MARRIAGES: Map<Suit, Int> = Suit.entries.associateWith { it.defaultMarriage }
        const val DEFAULT_ACE_MARRIAGE = 200
    }
}
