package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.Bid
import com.example.tysyacha_calc.data.model.CutRitualResult
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.PlayerState
import com.example.tysyacha_calc.data.model.PrikupShare
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.RoundEntry
import com.example.tysyacha_calc.data.model.ScoreEvent
import com.example.tysyacha_calc.data.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private fun game3(settings: GameSettings = GameSettings()) =
        GameEngine.newGame(settings.copy(playerCount = 3), listOf("А", "Б", "В"))

    private fun game4() =
        GameEngine.newGame(GameSettings(playerCount = 4), listOf("А", "Б", "В", "Г"))

    private fun GameState.withScores(vararg scores: Int) =
        copy(playerStates = playerStates.mapIndexed { i, s -> s.copy(score = scores[i]) })

    private fun GameState.input(
        taker: Int,
        bid: Int,
        vararg cards: Pair<Int, Int>,
        marriages: Map<Int, List<Suit>> = emptyMap(),
        prikup: Int? = null,
        cut: CutRitualResult? = null,
    ) = RoundInput(
        bid = Bid(taker, bid),
        entries = cards.map { (id, pts) -> RoundEntry(id, pts, marriages[id].orEmpty()) },
        prikup = prikup?.let { PrikupShare(dealerId, it) },
        cutRitual = cut,
    )

    private fun GameState.play(input: RoundInput): GameState {
        val outcome = GameEngine.playRound(this, input)
        check(outcome is RoundOutcome.Success) { "Кон не принят: $outcome" }
        return outcome.state
    }

    private fun GameState.errors(input: RoundInput): List<RoundError> =
        (GameEngine.playRound(this, input) as RoundOutcome.Invalid).errors

    private fun GameState.scores() = playerStates.map { it.score }

    // --- Подсчёт кона ---

    @Test fun takerFailsBidOthersGetTheirPoints() {
        val s = game3().play(game3().input(1, 100, 0 to 30, 1 to 70, 2 to 20))
        assertEquals(listOf(30, -100, 20), s.scores())
        assertEquals(2, s.nextRoundNumber)
        assertEquals(1, s.dealerId)
    }

    @Test fun takerWithMarriageGetsExactlyBid() {
        val g = game3()
        val s = g.play(g.input(1, 140, 0 to 40, 1 to 50, 2 to 30, marriages = mapOf(1 to listOf(Suit.HEARTS))))
        assertEquals(listOf(40, 140, 30), s.scores())
    }

    @Test fun defendersRoundedAndZeroIsDash() {
        val g = game3()
        val s = g.play(g.input(0, 85, 0 to 85, 1 to 33, 2 to 2).copy(bid = Bid(0, 100)))
        assertEquals(listOf(-100, 35, 0), s.scores())
        assertEquals(1, s.stateOf(2).dashCount)
        assertTrue(s.results.last().forPlayer(2).isDash)
    }

    @Test fun defenderMarriagesCount() {
        val g = game3()
        val s = g.play(g.input(0, 100, 0 to 100, 1 to 20, 2 to 0, marriages = mapOf(2 to listOf(Suit.SPADES))))
        assertEquals(40, s.stateOf(2).score)
        assertEquals(0, s.stateOf(2).dashCount)
    }

    @Test fun cardPointsMustSumTo120() {
        val g = game3()
        assertEquals(listOf(RoundError.CardPointsSum(110)), g.errors(g.input(1, 100, 0 to 30, 1 to 60, 2 to 20)))
    }

    @Test fun missingPlayerRejected() {
        val g = game3()
        assertTrue(RoundError.MissingEntry(2) in g.errors(g.input(1, 100, 0 to 30, 1 to 90)))
    }

    @Test fun bidValidation() {
        val g = game3()
        assertEquals(
            listOf(RoundError.BidTooLow(97), RoundError.BidNotMultipleOf5(97)),
            g.errors(g.input(1, 97, 0 to 30, 1 to 70, 2 to 20)),
        )
    }

    @Test fun marriageOfSuitOnlyOncePerRound() {
        val g = game3()
        val input = g.input(
            1, 100, 0 to 30, 1 to 70, 2 to 20,
            marriages = mapOf(0 to listOf(Suit.HEARTS), 1 to listOf(Suit.HEARTS)),
        )
        assertEquals(listOf(RoundError.DuplicateMarriage(Suit.HEARTS)), g.errors(input))
    }

    @Test fun cardLimitAndMismatch() {
        val g = game3()
        val input = RoundInput(
            bid = Bid(0, 100),
            entries = listOf(
                RoundEntry(0, 33, cards = mapOf(Rank.ACE to 3)),
                RoundEntry(1, 22, cards = mapOf(Rank.ACE to 2)),
                RoundEntry(2, 65),
            ),
        )
        val errors = g.errors(input)
        assertTrue(RoundError.CardLimitExceeded(Rank.ACE) in errors)
        assertTrue(RoundError.CardsMismatch(1) !in errors)
        assertEquals(listOf(RoundError.CardLimitExceeded(Rank.ACE)), errors)
        val mismatch = input.copy(entries = input.entries.toMutableList().also { it[1] = RoundEntry(1, 20, cards = mapOf(Rank.ACE to 1)) })
        assertTrue(RoundError.CardsMismatch(1) in g.errors(mismatch))
    }

    @Test fun longRoundTakerGetsAllOthersBolts() {
        val g = game3()
        val s = g.play(g.input(1, 120, 0 to 0, 1 to 120, 2 to 0).copy(isLong = true))
        assertEquals(listOf(0, 120, 0), s.scores())
        assertEquals(1, s.stateOf(0).dashCount)
        assertTrue(s.rounds.last().isLong)
        assertTrue(ScoreEvent.TakerMade(120, 120, isLong = true) in s.results.last().forPlayer(1).events)
    }

    @Test fun longRoundRequiresAllCardsAtTaker() {
        val g = game3()
        assertEquals(listOf(RoundError.InvalidLong), g.errors(g.input(1, 120, 0 to 10, 1 to 110, 2 to 0).copy(isLong = true)))
    }

    // --- Прочерки, сдвиг, 555, минус ---

    @Test fun threeDashesAnywhereInGame() {
        var s = game3()
        s = s.play(s.input(0, 100, 0 to 120, 1 to 0, 2 to 0))     // прочерк 1
        s = s.play(s.input(2, 100, 0 to 0, 1 to 20, 2 to 100))    // +20
        s = s.play(s.input(2, 100, 0 to 10, 1 to 0, 2 to 110))    // прочерк 2
        assertEquals(2, s.stateOf(1).dashCount)
        s = s.play(s.input(0, 100, 0 to 110, 1 to 0, 2 to 10))    // прочерк 3 → −120
        assertEquals(0, s.stateOf(1).dashCount)
        assertEquals(20 - 120, s.stateOf(1).score)
        assertTrue(ScoreEvent.DashPenalty(-120) in s.results.last().forPlayer(1).events)
    }

    @Test fun cutRitualPenalties() {
        // Кон 1: дилер 0, сдвигает игрок справа — 2.
        val g = game3()
        val s = g.play(g.input(1, 100, 0 to 10, 1 to 100, 2 to 10, cut = CutRitualResult(ninesCount = 6, jacksCount = 3)))
        assertEquals(listOf(10 - 120, 100, 10 - 240), s.scores())
    }

    @Test fun cutRitualRejectedWhenDisabled() {
        val g = game3(GameSettings(cutRitualEnabled = false))
        assertEquals(
            listOf(RoundError.CutRitualDisabled),
            g.errors(g.input(1, 100, 0 to 10, 1 to 100, 2 to 10, cut = CutRitualResult(3, 0))),
        )
    }

    @Test fun rule555() {
        val g = game3().withScores(435, 0, 0)
        val s = g.play(g.input(0, 120, 0 to 120, 1 to 0, 2 to 0))
        assertEquals(0, s.stateOf(0).score)
        assertTrue(ScoreEvent.Reset555(555) in s.results.last().forPlayer(0).events)
    }

    @Test fun rule555CanBeDisabled() {
        val g = game3(GameSettings(use555Rule = false)).withScores(435, 0, 0)
        val s = g.play(g.input(0, 120, 0 to 120, 1 to 0, 2 to 0))
        assertEquals(555, s.stateOf(0).score)
    }

    @Test fun minus555() {
        val g = game3().withScores(-435, 0, 0)
        val s = g.play(g.input(0, 120, 0 to 100, 1 to 10, 2 to 10))
        assertEquals(0, s.stateOf(0).score)
    }

    @Test fun noNegativeScoreClampsAtZero() {
        val g = game3(GameSettings(allowNegativeScore = false)).withScores(50, 0, 0)
        val s = g.play(g.input(0, 120, 0 to 100, 1 to 10, 2 to 10))
        assertEquals(0, s.stateOf(0).score)
        assertTrue(ScoreEvent.ClampedAtZero(70) in s.results.last().forPlayer(0).events)
    }

    // --- Бочка ---

    @Test fun bochkaFullCycleWithPenalty() {
        var s = game3().withScores(800, 0, 0)
        s = s.play(s.input(0, 100, 0 to 100, 1 to 10, 2 to 10))
        assertEquals(880, s.stateOf(0).score)
        assertTrue(s.stateOf(0).isOnBochka)
        // Три кона без пробития — попытки тикают, даже если игрок ничего не делал.
        s = s.play(s.input(1, 100, 0 to 15, 1 to 100, 2 to 5))
        s = s.play(s.input(1, 100, 0 to 10, 1 to 100, 2 to 10))
        assertEquals(1, s.stateOf(0).bochkaAttemptsLeft)
        s = s.play(s.input(1, 100, 0 to 10, 1 to 100, 2 to 10))
        assertEquals(760, s.stateOf(0).score)
        assertFalse(s.stateOf(0).isOnBochka)
    }

    @Test fun bochkaBrokenWins() {
        var s = game3().withScores(880, 0, 0)
        s = s.copy(playerStates = s.playerStates.toMutableList().also {
            it[0] = PlayerState(0, 880, isOnBochka = true, bochkaAttemptsLeft = 3)
        })
        s = s.play(s.input(0, 120, 0 to 120, 1 to 0, 2 to 0))
        assertEquals(1000, s.stateOf(0).score)
        assertEquals(0, s.winnerId)
        assertEquals(listOf(RoundError.GameFinished), s.errors(s.input(1, 100)))
    }

    @Test fun failedBidDropsFromBochka() {
        var s = game3()
        s = s.copy(playerStates = s.playerStates.toMutableList().also {
            it[0] = PlayerState(0, 880, isOnBochka = true, bochkaAttemptsLeft = 2)
        })
        s = s.play(s.input(0, 110, 0 to 100, 1 to 10, 2 to 10))
        assertEquals(770, s.stateOf(0).score)
        assertFalse(s.stateOf(0).isOnBochka)
    }

    // --- Роспись ---

    @Test fun rospisSplitsHalfToOtherTwo() {
        val g = game3()
        val s = g.play(RoundInput(Bid(takerId = 1, amount = 125), isRospis = true))
        assertEquals(listOf(65, 0, 65), s.scores())
        assertEquals(0, s.stateOf(1).dashCount)
        assertEquals(1, s.rospisUsedBy(1))
        assertEquals(2, s.rospisLeftFor(1))
    }

    @Test fun rospisLimit() {
        var s = game3()
        repeat(3) { s = s.play(RoundInput(Bid(0, 100), isRospis = true)) }
        assertEquals(listOf(RoundError.RospisLimitReached), s.errors(RoundInput(Bid(0, 100), isRospis = true)))
        // У других игроков лимит свой.
        s = s.play(RoundInput(Bid(1, 100), isRospis = true))
        assertEquals(2, s.rospisLeftFor(1))
    }

    // --- Четверо ---

    @Test fun fourPlayersDealerGetsPrikup() {
        // Кон 1: дилер 0, играют 1, 2, 3.
        val g = game4()
        assertEquals(1, g.firstBidderId)
        val s = g.play(g.input(1, 100, 1 to 100, 2 to 18, 3 to 2, prikup = 23))
        assertEquals(listOf(25, 100, 20, 0), s.scores())
        assertEquals(1, s.stateOf(3).dashCount)
        assertEquals(listOf(0, 2, 3), s.activePlayerIds) // кон 2: дилер 1
    }

    @Test fun fourPlayersDealerCannotPlayOrTake() {
        val g = game4()
        val errors = g.errors(g.input(0, 100, 0 to 0, 1 to 100, 2 to 10, 3 to 10))
        assertTrue(RoundError.UnexpectedEntry(0) in errors)
        assertTrue(RoundError.TakerNotActive(0) in errors)
    }

    @Test fun fourPlayersRospisGoesToOtherTwoActive() {
        val g = game4()
        val s = g.play(RoundInput(Bid(2, 100), prikup = PrikupShare(0, 21), isRospis = true))
        assertEquals(listOf(20, 50, 0, 50), s.scores())
    }

    @Test fun prikupOnlyFor4() {
        val g = game3()
        assertEquals(listOf(RoundError.PrikupNotAllowed), g.errors(g.input(1, 100, 0 to 10, 1 to 100, 2 to 10, prikup = 10)))
    }

    @Test fun undoAndReplayGiveSameState() {
        var s = game3()
        s = s.play(s.input(0, 100, 0 to 120, 1 to 0, 2 to 0))
        val afterFirst = s
        s = s.play(RoundInput(Bid(2, 110), isRospis = true, cutRitual = CutRitualResult(3, 3)))
        assertEquals(s, GameEngine.replay(s.toSession()))
        assertEquals(afterFirst, GameEngine.undoLastRound(s))
        assertTrue(GameEngine.undoLastRound(afterFirst).rounds.isEmpty())
    }
}
