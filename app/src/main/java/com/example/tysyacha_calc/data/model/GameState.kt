package com.example.tysyacha_calc.data.model

import com.example.tysyacha_calc.domain.DealerRotation
import com.example.tysyacha_calc.domain.GameRules

/** Полное вычисленное состояние партии. Получается только через GameEngine. */
data class GameState(
    val settings: GameSettings,
    val players: List<Player>,
    val playerStates: List<PlayerState>,    // в том же порядке, что и players
    val rounds: List<GameRound> = emptyList(),
    val results: List<RoundResult> = emptyList(),
    val winnerId: Int? = null,
) {
    val nextRoundNumber: Int get() = rounds.size + 1
    val dealerIndex: Int get() = DealerRotation.dealerIndex(nextRoundNumber, players.size)
    val dealerId: Int get() = players[dealerIndex].id
    val cutterId: Int get() = players[DealerRotation.cutterIndex(dealerIndex, players.size)].id
    /** Первый после раздающего — на нём автоматическая ставка 100. */
    val firstBidderId: Int get() = players[DealerRotation.firstBidderIndex(dealerIndex, players.size)].id
    /** Кто реально играет в этом кону (при 4 игроках — все, кроме раздающего). */
    val activePlayerIds: List<Int>
        get() = DealerRotation.activeIndices(dealerIndex, players.size).map { players[it].id }
    /** Раздающий в прикупе (только при 4 игроках). */
    val prikupPlayerId: Int? get() = if (DealerRotation.dealerHasShare(players.size)) dealerId else null

    /** Сколько раз игрок расписывался (лимит — у каждого свой). */
    fun rospisUsedBy(playerId: Int): Int = rounds.count { it.isRospis && it.bid.takerId == playerId }
    fun rospisLeftFor(playerId: Int): Int = GameRules.ROSPIS_LIMIT - rospisUsedBy(playerId)
    val isFinished: Boolean get() = winnerId != null

    fun player(id: Int): Player = players.first { it.id == id }
    fun stateOf(id: Int): PlayerState = playerStates.first { it.playerId == id }

    fun toSession(): GameSession = GameSession(settings, players, rounds)
}
