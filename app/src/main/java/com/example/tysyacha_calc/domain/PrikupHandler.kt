package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.PrikupShare
import com.example.tysyacha_calc.data.model.Rank

/**
 * Игра вчетвером: раздающий не участвует в розыгрыше и записывает себе
 * очки трёх карт прикупа (округлённые до 5). Трое играющих делят все 120 очков колоды.
 */
object PrikupHandler {

    fun isPrikupMode(playerCount: Int): Boolean = DealerRotation.dealerHasShare(playerCount)

    /** Максимум очков в прикупе: туз, туз, туз. */
    val maxPoints: Int = Rank.ACE.points * GameRules.PRIKUP_CARDS

    fun canAddCard(cards: Map<Rank, Int>, rank: Rank): Boolean =
        CardPickValidator.countOf(cards) < GameRules.PRIKUP_CARDS && (cards[rank] ?: 0) < Rank.COPIES

    fun isValid(share: PrikupShare): Boolean =
        share.cardPoints in 0..maxPoints &&
            CardPickValidator.countOf(share.cards) <= GameRules.PRIKUP_CARDS &&
            (share.cards.isEmpty() || CardPickValidator.pointsOf(share.cards) == share.cardPoints)

    fun score(share: PrikupShare): Int = ScoreRoundingUtil.roundTo5(share.cardPoints)
}
