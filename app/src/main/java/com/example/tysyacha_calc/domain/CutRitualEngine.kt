package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.CutRitualResult

enum class CutCard { NINE, JACK, OTHER }

object CutRitualEngine {

    /** −120 за каждые полные 3 карты. */
    fun penaltyFor(count: Int): Int = -GameRules.PENALTY * (count / GameRules.CUT_CARDS_LIMIT)

    /**
     * Последовательность нижних карт при сдвиге. Девятки и валеты считаются независимо,
     * ритуал заканчивается на первой «другой» карте; после неё карт быть не должно.
     */
    fun fromSequence(cards: List<CutCard>): CutRitualResult {
        val otherIndex = cards.indexOf(CutCard.OTHER)
        require(otherIndex == -1 || otherIndex == cards.lastIndex) {
            "После обычной карты ритуал сдвига уже закончен"
        }
        return CutRitualResult(
            ninesCount = cards.count { it == CutCard.NINE },
            jacksCount = cards.count { it == CutCard.JACK },
        )
    }

    fun add(result: CutRitualResult, card: CutCard): CutRitualResult = when (card) {
        CutCard.NINE -> result.copy(ninesCount = result.ninesCount + 1)
        CutCard.JACK -> result.copy(jacksCount = result.jacksCount + 1)
        CutCard.OTHER -> result
    }
}
