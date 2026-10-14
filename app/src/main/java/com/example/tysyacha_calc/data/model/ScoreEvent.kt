package com.example.tysyacha_calc.data.model

/** Что произошло со счётом игрока за кон — для истории и предпросмотра. */
sealed interface ScoreEvent {
    /** Взявший прикуп набрал [total] при ставке [bid]. */
    data class TakerMade(val total: Int, val bid: Int, val isLong: Boolean = false) : ScoreEvent
    data class TakerFailed(val total: Int, val bid: Int) : ScoreEvent
    /** Не бравший прикуп набрал [total], записано [points] (округлено до 5). */
    data class Defender(val total: Int, val points: Int) : ScoreEvent
    /** Набрал 0 — болт. */
    data class Dash(val dashCount: Int) : ScoreEvent
    data class Prikup(val total: Int, val points: Int) : ScoreEvent
    data class RospisTaker(val bid: Int) : ScoreEvent
    data class RospisBonus(val amount: Int) : ScoreEvent

    data class CutNinesPenalty(val nines: Int, val amount: Int) : ScoreEvent
    data class CutJacksPenalty(val jacks: Int, val amount: Int) : ScoreEvent
    data class DashPenalty(val amount: Int) : ScoreEvent

    /** Встал на бочку, [burned] очков сгорело. */
    data class BochkaEntered(val burned: Int) : ScoreEvent
    /** На бочке набрал меньше нужного: очки сгорели, попытка потрачена. */
    data class BochkaAttemptUsed(val burned: Int, val attemptsLeft: Int) : ScoreEvent
    data object BochkaBroken : ScoreEvent
    /** Упал с бочки ниже 880 (минус за кон или штраф). */
    data object BochkaLeft : ScoreEvent
    data class BochkaFailed(val amount: Int) : ScoreEvent

    /** Минус выключен: счёт упёрся в 0, [absorbed] очков не списалось. */
    data class ClampedAtZero(val absorbed: Int) : ScoreEvent
    data class Reset555(val from: Int) : ScoreEvent
}
