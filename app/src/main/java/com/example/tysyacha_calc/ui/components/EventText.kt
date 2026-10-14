package com.example.tysyacha_calc.ui.components

import com.example.tysyacha_calc.data.model.ScoreEvent
import com.example.tysyacha_calc.domain.GameRules

/** Человеческое описание события кона. */
fun ScoreEvent.describe(): String = when (this) {
    is ScoreEvent.TakerMade ->
        if (isLong) "Длинная! Ставка $bid → +$bid" else "Ставка $bid сыграна: $total → +$bid"
    is ScoreEvent.TakerFailed -> "Ставка $bid не сыграна: $total → −$bid"
    is ScoreEvent.Defender -> if (total == points) "Итого $total" else "Итого $total → $points"
    is ScoreEvent.Dash -> "Болт ($dashCount из ${GameRules.DASH_LIMIT})"
    is ScoreEvent.Prikup -> if (total == points) "Прикуп: $total" else "Прикуп: $total → $points"
    is ScoreEvent.RospisTaker -> "Роспись на $bid"
    is ScoreEvent.RospisBonus -> "Роспись: +$amount"
    is ScoreEvent.CutNinesPenalty -> "Девятки при сдвиге ($nines): ${formatDelta(amount)}"
    is ScoreEvent.CutJacksPenalty -> "Вальты при сдвиге ($jacks): ${formatDelta(amount)}"
    is ScoreEvent.DashPenalty -> "Три болта: ${formatDelta(amount)}"
    is ScoreEvent.BochkaEntered ->
        if (burned > 0) "На бочке, сгорело $burned" else "На бочке"
    is ScoreEvent.BochkaAttemptUsed ->
        "На бочке: сгорело $burned, попыток осталось $attemptsLeft"
    ScoreEvent.BochkaBroken -> "Бочка пробита!"
    ScoreEvent.BochkaLeft -> "Бочка снята"
    is ScoreEvent.BochkaFailed -> "Бочка не пробита: ${formatDelta(amount)}"
    is ScoreEvent.ClampedAtZero -> "Ниже нуля нельзя: не списано $absorbed"
    is ScoreEvent.Reset555 -> "Ровно ${formatScore(from)} — сброс в 0!"
}

/** События, которые стоит подсветить (штрафы и особые правила), а не обычный подсчёт очков. */
val ScoreEvent.isNotable: Boolean
    get() = when (this) {
        is ScoreEvent.TakerMade, is ScoreEvent.TakerFailed, is ScoreEvent.Defender,
        is ScoreEvent.Prikup, is ScoreEvent.RospisBonus, is ScoreEvent.RospisTaker -> false
        else -> true
    }
