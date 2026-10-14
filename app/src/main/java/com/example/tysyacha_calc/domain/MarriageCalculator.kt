package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.RoundEntry
import com.example.tysyacha_calc.data.model.Suit

/**
 * Марьяжи. Король и дама каждой масти в колоде одни, поэтому марьяж масти
 * за кон может объявить только один игрок; тузовый (все 4 туза) — тоже только один.
 */
object MarriageCalculator {

    fun points(marriages: List<Suit>, hasAceMarriage: Boolean, settings: GameSettings): Int =
        marriages.sumOf { settings.marriageValue(it) } +
            if (hasAceMarriage) settings.aceMarriageValue else 0

    fun points(entry: RoundEntry, settings: GameSettings): Int =
        points(entry.marriages, entry.hasAceMarriage, settings)

    /** Масти, объявленные больше одного раза за кон. */
    fun duplicatedSuits(entries: List<RoundEntry>): Set<Suit> =
        entries.flatMap { it.marriages }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

    fun isAceMarriageDuplicated(entries: List<RoundEntry>): Boolean =
        entries.count { it.hasAceMarriage } > 1

    /** Кто уже объявил марьяж этой масти (чтобы заблокировать кнопку у остальных). */
    fun ownerOf(suit: Suit, entries: List<RoundEntry>): Int? =
        entries.firstOrNull { suit in it.marriages }?.playerId

    fun aceMarriageOwner(entries: List<RoundEntry>): Int? =
        entries.firstOrNull { it.hasAceMarriage }?.playerId
}
