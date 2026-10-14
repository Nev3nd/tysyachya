package com.example.tysyacha_calc.viewmodel

import com.example.tysyacha_calc.data.model.Bid
import com.example.tysyacha_calc.data.model.CutRitualResult
import com.example.tysyacha_calc.data.model.GameRound
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.PrikupShare
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.RoundEntry
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.domain.CardPickValidator
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.domain.RoundInput
import kotlinx.serialization.Serializable

/** Ввод одного игрока в незавершённом кону. */
@Serializable
data class HandDraft(
    val pointsText: String = "0",
    val cards: Map<Rank, Int> = emptyMap(),
    val marriages: List<Suit> = emptyList(),
    val hasAceMarriage: Boolean = false,
) {
    /** Очки картами: по выбранным картам или числом (пусто = 0). */
    val cardPoints: Int
        get() = if (cards.isNotEmpty()) CardPickValidator.pointsOf(cards) else pointsText.toIntOrNull() ?: 0
}

/**
 * Незавершённый кон. Сериализуется в SavedStateHandle, чтобы ввод
 * не терялся ни при повороте экрана, ни при выгрузке процесса.
 *
 * Очки вводятся только для тех, кто не брал прикуп; взявшему достаётся остаток от 120.
 */
@Serializable
data class RoundDraft(
    val roundNumber: Int,
    /** Кто взял прикуп; null — торги ещё не закончились. */
    val takerId: Int? = null,
    val bid: Int = GameRules.MIN_BID,
    val isRospis: Boolean = false,
    val isLong: Boolean = false,
    val nines: Int = 0,
    val jacks: Int = 0,
    val hands: Map<Int, HandDraft> = emptyMap(),
    val prikupText: String = "0",
    val prikupCards: Map<Rank, Int> = emptyMap(),
) {
    fun hand(playerId: Int): HandDraft = hands[playerId] ?: HandDraft()

    fun updateHand(playerId: Int, transform: (HandDraft) -> HandDraft): RoundDraft =
        copy(hands = hands + (playerId to transform(hand(playerId))))

    val prikupPoints: Int
        get() = if (prikupCards.isNotEmpty()) CardPickValidator.pointsOf(prikupCards) else prikupText.toIntOrNull() ?: 0

    fun defenders(game: GameState): List<Int> = turnOrder(game).filter { it != takerId }

    /** Очки картами игрока, не бравшего прикуп (при «длинной» — 0). */
    fun defenderPoints(playerId: Int): Int = if (isLong) 0 else hand(playerId).cardPoints

    /** Взявшему прикуп — всё, что не взяли остальные. */
    fun takerCardPoints(game: GameState): Int =
        if (isLong) GameRules.CARD_POINTS_TOTAL
        else GameRules.CARD_POINTS_TOTAL - defenders(game).sumOf { defenderPoints(it) }

    /** Больше этого игроку [playerId] не набрать: остальные уже забрали своё. */
    fun maxPointsFor(playerId: Int, game: GameState): Int =
        GameRules.CARD_POINTS_TOTAL - defenders(game).filter { it != playerId }.sumOf { defenderPoints(it) }

    /** Готовый ввод для движка или null, если не выбран взявший прикуп. */
    fun toInput(game: GameState): RoundInput? {
        val taker = takerId ?: return null
        val cut = if (game.settings.cutRitualEnabled) CutRitualResult(nines, jacks) else null
        val prikup = game.prikupPlayerId?.let { PrikupShare(it, prikupPoints, prikupCards) }
        val bid = Bid(taker, bid)
        if (isRospis) return RoundInput(bid, emptyList(), prikup, cut, isRospis = true)

        val entries = game.activePlayerIds.map { id ->
            val h = hand(id)
            when {
                id == taker -> RoundEntry(id, takerCardPoints(game), h.marriages, h.hasAceMarriage, isAuto = true)
                isLong -> RoundEntry(id, 0)
                else -> RoundEntry(id, h.cardPoints, h.marriages, h.hasAceMarriage, h.cards)
            }
        }
        return RoundInput(bid, entries, prikup, cut, isLong = isLong)
    }

    companion object {
        fun initial(game: GameState): RoundDraft = RoundDraft(roundNumber = game.nextRoundNumber)

        /** Сохранённый кон обратно в форму (когда вернулись к нему по истории). */
        fun fromRound(round: GameRound): RoundDraft {
            val taker = round.bid.takerId
            return RoundDraft(
                roundNumber = round.roundNumber,
                takerId = taker,
                bid = round.bid.amount,
                isRospis = round.isRospis,
                isLong = round.isLong,
                nines = round.cutRitualResult?.ninesCount ?: 0,
                jacks = round.cutRitualResult?.jacksCount ?: 0,
                hands = round.entries.associate { e ->
                    e.playerId to HandDraft(
                        pointsText = if (e.playerId == taker || e.cards.isNotEmpty()) "0" else e.cardPoints.toString(),
                        cards = if (e.playerId == taker) emptyMap() else e.cards,
                        marriages = e.marriages,
                        hasAceMarriage = e.hasAceMarriage,
                    )
                },
                prikupText = round.prikup?.takeIf { it.cards.isEmpty() }?.cardPoints?.toString() ?: "0",
                prikupCards = round.prikup?.cards.orEmpty(),
            )
        }

        /** Играющие в порядке хода, начиная с первого после раздающего. */
        fun turnOrder(game: GameState): List<Int> {
            val ids = game.players.map { it.id }
            val start = ids.indexOf(game.firstBidderId)
            return ids.indices.map { ids[(start + it) % ids.size] }.filter { it in game.activePlayerIds }
        }
    }
}
