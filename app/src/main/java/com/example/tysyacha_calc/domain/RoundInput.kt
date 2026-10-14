package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.Bid
import com.example.tysyacha_calc.data.model.CutRitualResult
import com.example.tysyacha_calc.data.model.PrikupShare
import com.example.tysyacha_calc.data.model.RoundEntry

/** Готовый к применению ввод кона (собирается экраном калькулятора). */
data class RoundInput(
    val bid: Bid,
    /** По записи на каждого играющего; при росписи — пусто. */
    val entries: List<RoundEntry> = emptyList(),
    /** Только при 4 игроках; null = 0 очков. */
    val prikup: PrikupShare? = null,
    /** null = ритуал закончился сразу (или правило выключено). */
    val cutRitual: CutRitualResult? = null,
    val isRospis: Boolean = false,
    /** «Длинная» — взявший прикуп забрал все карты. */
    val isLong: Boolean = false,
)
