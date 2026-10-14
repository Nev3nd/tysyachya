package com.example.tysyacha_calc.data.model

data class PlayerRoundResult(
    val playerId: Int,
    val scoreBefore: Int,
    val scoreAfter: Int,
    val events: List<ScoreEvent>,
) {
    val delta: Int get() = scoreAfter - scoreBefore
    val isDash: Boolean get() = events.any { it is ScoreEvent.Dash }
}

data class RoundResult(
    val round: GameRound,
    val players: List<PlayerRoundResult>,
) {
    fun forPlayer(playerId: Int): PlayerRoundResult = players.first { it.playerId == playerId }
}
