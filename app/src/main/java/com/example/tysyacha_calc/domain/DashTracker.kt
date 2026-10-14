package com.example.tysyacha_calc.domain

/** Болты копятся за всю партию (не обязательно подряд); на 3-м — обнуление и −120. В коде болт = dash. */
object DashTracker {

    data class Result(val dashCount: Int, val penalty: Int)

    fun registerDash(dashCount: Int): Result {
        val newCount = dashCount + 1
        return if (newCount >= GameRules.DASH_LIMIT) Result(0, -GameRules.PENALTY)
        else Result(newCount, 0)
    }
}
