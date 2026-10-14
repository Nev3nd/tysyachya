package com.example.tysyacha_calc.viewmodel

import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.domain.GameEngine
import com.example.tysyacha_calc.domain.RoundOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundDraftTest {

    private val game = GameEngine.newGame(GameSettings(), listOf("А", "Б", "В"))

    @Test fun noTakerNoInput() {
        assertNull(RoundDraft.initial(game).toInput(game))
    }

    @Test fun defaultsAreZeroTakerGetsAll() {
        val d = RoundDraft.initial(game).copy(takerId = 1)
        assertEquals(120, d.takerCardPoints(game))
        val outcome = GameEngine.playRound(game, d.toInput(game)!!)
        assertTrue(outcome is RoundOutcome.Success)
    }

    @Test fun takerGetsRemainder() {
        val d = RoundDraft.initial(game).copy(takerId = 1)
            .updateHand(0) { it.copy(pointsText = "35") }
            .updateHand(2) { it.copy(cards = mapOf(Rank.ACE to 1, Rank.TEN to 1)) }
        assertEquals(120 - 35 - 21, d.takerCardPoints(game))
        assertEquals(120 - 21, d.maxPointsFor(0, game))
    }

    @Test fun longZeroesDefenders() {
        val d = RoundDraft.initial(game).copy(takerId = 1, isLong = true)
            .updateHand(0) { it.copy(pointsText = "35", marriages = listOf(Suit.HEARTS)) }
        val input = d.toInput(game)!!
        assertEquals(listOf(0, 120, 0), input.entries.map { it.cardPoints })
        assertTrue(input.entries.first { it.playerId == 0 }.marriages.isEmpty())
    }

    @Test fun roundTripThroughHistory() {
        val d = RoundDraft.initial(game).copy(takerId = 2, bid = 130)
            .updateHand(0) { it.copy(pointsText = "40") }
            .updateHand(1) { it.copy(cards = mapOf(Rank.KING to 2)) }
            .updateHand(2) { it.copy(marriages = listOf(Suit.DIAMONDS)) }
        val round = (GameEngine.playRound(game, d.toInput(game)!!) as RoundOutcome.Success).result.round
        val restored = RoundDraft.fromRound(round)
        val again = (GameEngine.playRound(game, restored.toInput(game)!!) as RoundOutcome.Success).result.round
        assertEquals(round, again)
    }
}
