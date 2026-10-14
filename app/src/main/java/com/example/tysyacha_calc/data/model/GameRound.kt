package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/** Сохранённый кон — то, что реально лежит в истории партии. Итоговые очки из него вычисляются. */
@Serializable
data class GameRound(
    val roundNumber: Int,
    val dealerId: Int,
    val cutRitualResult: CutRitualResult?,  // null, если правило выключено
    val bid: Bid,
    /** Игравшие в кону. При росписи — пусто: кон не разыгрывался. */
    val entries: List<RoundEntry>,
    /** Доля раздающего при 4 игроках, иначе null. */
    val prikup: PrikupShare? = null,
    val isRospis: Boolean = false,
    /** «Длинная»: взявший прикуп забрал все взятки (120 картами), у остальных 0. */
    val isLong: Boolean = false,
)
