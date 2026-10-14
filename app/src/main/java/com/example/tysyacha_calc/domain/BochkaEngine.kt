package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.PlayerState
import com.example.tysyacha_calc.data.model.ScoreEvent

/**
 * Бочка (880). Работает в две фазы внутри кона:
 * 1. [applyPoints] — очки за кон (свои, доля дилера, половина росписи) проходят через «стену» 880;
 * 2. [resolve] — после штрафов: если игрок слетел с 880 — бочка снята,
 *    если попытки кончились — −120.
 *
 * Инвариант: isOnBochka == true ⇒ score == 880.
 */
object BochkaEngine {

    data class Result(val state: PlayerState, val events: List<ScoreEvent>)

    fun applyPoints(state: PlayerState, points: Int): Result {
        if (state.isOnBochka) {
            return when {
                points >= GameRules.BOCHKA_BREAK_POINTS -> Result(
                    state.copy(score = state.score + points, isOnBochka = false, bochkaAttemptsLeft = 0),
                    listOf(ScoreEvent.BochkaBroken),
                )
                points < 0 -> Result(
                    state.copy(score = state.score + points, isOnBochka = false, bochkaAttemptsLeft = 0),
                    listOf(ScoreEvent.BochkaLeft),
                )
                else -> {
                    val left = state.bochkaAttemptsLeft - 1
                    Result(
                        state.copy(bochkaAttemptsLeft = left),
                        listOf(ScoreEvent.BochkaAttemptUsed(burned = points, attemptsLeft = left)),
                    )
                }
            }
        }

        val newScore = state.score + points
        if (state.score < GameRules.BOCHKA_SCORE && newScore >= GameRules.BOCHKA_SCORE) {
            return Result(
                state.copy(
                    score = GameRules.BOCHKA_SCORE,
                    isOnBochka = true,
                    bochkaAttemptsLeft = GameRules.BOCHKA_ATTEMPTS,
                ),
                listOf(ScoreEvent.BochkaEntered(burned = newScore - GameRules.BOCHKA_SCORE)),
            )
        }
        return Result(state.copy(score = newScore), emptyList())
    }

    fun resolve(state: PlayerState): Result {
        if (!state.isOnBochka) return Result(state, emptyList())

        if (state.score != GameRules.BOCHKA_SCORE) {
            return Result(
                state.copy(isOnBochka = false, bochkaAttemptsLeft = 0),
                listOf(ScoreEvent.BochkaLeft),
            )
        }
        if (state.bochkaAttemptsLeft <= 0) {
            return Result(
                state.copy(
                    score = state.score - GameRules.PENALTY,
                    isOnBochka = false,
                    bochkaAttemptsLeft = 0,
                ),
                listOf(ScoreEvent.BochkaFailed(-GameRules.PENALTY)),
            )
        }
        return Result(state, emptyList())
    }
}
