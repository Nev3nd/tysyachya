package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.Rank

/** Набор очков кнопками-картами: за кон у всех вместе не больше 4 карт одного номинала. */
object CardPickValidator {

    fun pointsOf(cards: Map<Rank, Int>): Int = cards.entries.sumOf { (rank, n) -> rank.points * n }

    fun countOf(cards: Map<Rank, Int>): Int = cards.values.sum()

    fun totalByRank(hands: List<Map<Rank, Int>>): Map<Rank, Int> =
        Rank.entries.associateWith { rank -> hands.sumOf { it[rank] ?: 0 } }

    /** Сколько карт номинала [rank] ещё можно выбрать. */
    fun remaining(rank: Rank, hands: List<Map<Rank, Int>>): Int =
        Rank.COPIES - hands.sumOf { it[rank] ?: 0 }

    fun canAdd(rank: Rank, hands: List<Map<Rank, Int>>): Boolean = remaining(rank, hands) > 0

    /** Номиналы, которых выбрано больше 4. */
    fun exceededRanks(hands: List<Map<Rank, Int>>): List<Rank> =
        totalByRank(hands).filterValues { it > Rank.COPIES }.keys.toList()

    fun add(cards: Map<Rank, Int>, rank: Rank): Map<Rank, Int> =
        cards + (rank to (cards[rank] ?: 0) + 1)

    fun remove(cards: Map<Rank, Int>, rank: Rank): Map<Rank, Int> {
        val n = (cards[rank] ?: 0) - 1
        return if (n <= 0) cards - rank else cards + (rank to n)
    }
}
