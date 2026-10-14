package com.example.tysyacha_calc.domain

/** Роспись: у каждого игрока не больше 3 за партию, двум другим игрокам тройки — половина ставки, округлённая до 5. */
object RospisEngine {

    fun canRospis(rospisUsed: Int): Boolean = rospisUsed < GameRules.ROSPIS_LIMIT

    fun splitAmount(stake: Int): Int = ScoreRoundingUtil.roundTo5(stake / 2.0)

    /** Кому начисляется половина: активные игроки кона, кроме взявшего прикуп. */
    fun recipients(activePlayerIds: List<Int>, takerId: Int): List<Int> =
        activePlayerIds.filter { it != takerId }
}
