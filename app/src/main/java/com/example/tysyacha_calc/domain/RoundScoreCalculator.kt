package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.RoundEntry

/**
 * Очки за кон:
 * - взявший прикуп: карты + марьяжи ≥ ставки → +ставка, иначе −ставка (без округления — ставка кратна 5);
 * - остальные: карты + марьяжи, округлённые до ближайших 5.
 */
object RoundScoreCalculator {

    /** Очки последнего игрока по остатку от 120. */
    fun autoCardPoints(otherCardPoints: List<Int>): Int =
        GameRules.CARD_POINTS_TOTAL - otherCardPoints.sum()

    fun total(entry: RoundEntry, settings: GameSettings): Int =
        entry.cardPoints + MarriageCalculator.points(entry, settings)

    fun isBidMade(total: Int, bid: Int): Boolean = total >= bid

    fun takerScore(total: Int, bid: Int): Int = if (isBidMade(total, bid)) bid else -bid

    fun defenderScore(total: Int): Int = ScoreRoundingUtil.roundTo5(total)

    /** Максимальная разумная ставка: все карты плюс все марьяжи. */
    fun maxBid(settings: GameSettings): Int =
        GameRules.CARD_POINTS_TOTAL + settings.marriageValues.values.sum() + settings.aceMarriageValue
}
