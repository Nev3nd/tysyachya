package com.example.tysyacha_calc.domain

import kotlin.math.abs

/** Счёт ровно +555 или −555 сбрасывается до 0. Правило можно выключить в настройках партии. */
object Rule555 {
    fun isTriggered(score: Int): Boolean = abs(score) == GameRules.RESET_SCORE

    fun apply(score: Int): Int = if (isTriggered(score)) 0 else score

    /** Сколько осталось до ближайшего порога ±555 (для «прогресса до 555»), null — если порог уже позади. */
    fun distance(score: Int): Int? = when {
        score in 0 until GameRules.RESET_SCORE -> GameRules.RESET_SCORE - score
        score < 0 && score > -GameRules.RESET_SCORE -> score + GameRules.RESET_SCORE
        else -> null
    }
}
