package com.example.tysyacha_calc.domain

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign

object ScoreRoundingUtil {

    /** Округление до ближайших 5; ровно посередине — от нуля (62.5 → 65, −62.5 → −65). */
    fun roundTo5(value: Double): Int {
        val step = GameRules.SCORE_STEP
        val rounded = floor(abs(value) / step + 0.5).toInt() * step
        return (sign(value) * rounded).toInt()
    }

    fun roundTo5(value: Int): Int = roundTo5(value.toDouble())

    fun isMultipleOf5(value: Int): Boolean = value % GameRules.SCORE_STEP == 0
}
