package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.CutRitualResult
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.RoundEntry
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.data.model.PlayerState
import com.example.tysyacha_calc.data.model.ScoreEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreRoundingUtilTest {
    @Test fun roundsToNearest5() {
        assertEquals(35, ScoreRoundingUtil.roundTo5(37))
        assertEquals(40, ScoreRoundingUtil.roundTo5(38))
        assertEquals(120, ScoreRoundingUtil.roundTo5(120))
        assertEquals(0, ScoreRoundingUtil.roundTo5(2))
        assertEquals(-35, ScoreRoundingUtil.roundTo5(-37))
    }

    @Test fun halfRoundsAwayFromZero() {
        assertEquals(65, ScoreRoundingUtil.roundTo5(62.5))
        assertEquals(-65, ScoreRoundingUtil.roundTo5(-62.5))
    }
}

class DealerRotationTest {
    @Test fun dealerMovesAroundTheTable() {
        assertEquals(listOf(0, 1, 2, 0), (1..4).map { DealerRotation.dealerIndex(it, 3) })
        assertEquals(listOf(0, 1, 2, 3, 0), (1..5).map { DealerRotation.dealerIndex(it, 4) })
    }

    @Test fun cutterIsToTheRightOfDealer() {
        assertEquals(2, DealerRotation.cutterIndex(0, 3))
        assertEquals(0, DealerRotation.cutterIndex(1, 3))
        assertEquals(3, DealerRotation.cutterIndex(0, 4))
    }

    @Test fun activePlayers() {
        assertEquals(listOf(0, 1, 2), DealerRotation.activeIndices(1, 3))
        assertEquals(listOf(0, 2, 3), DealerRotation.activeIndices(1, 4))
    }
}

class CutRitualEngineTest {
    @Test fun countersAreIndependent() {
        val r = CutRitualEngine.fromSequence(
            listOf(CutCard.NINE, CutCard.JACK, CutCard.NINE, CutCard.JACK, CutCard.NINE, CutCard.OTHER)
        )
        assertEquals(CutRitualResult(ninesCount = 3, jacksCount = 2), r)
        assertEquals(-120, r.cutterPenalty)
        assertEquals(0, r.dealerPenalty)
    }

    @Test fun penaltyPerFullThree() {
        assertEquals(0, CutRitualEngine.penaltyFor(2))
        assertEquals(-120, CutRitualEngine.penaltyFor(5))
        assertEquals(-240, CutRitualEngine.penaltyFor(6))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noCardsAfterOther() {
        CutRitualEngine.fromSequence(listOf(CutCard.OTHER, CutCard.NINE))
    }
}

class Rule555Test {
    @Test fun resetsOnExact555() {
        assertEquals(0, Rule555.apply(555))
        assertEquals(0, Rule555.apply(-555))
        assertEquals(560, Rule555.apply(560))
    }

    @Test fun distance() {
        assertEquals(555, Rule555.distance(0))
        assertEquals(35, Rule555.distance(520))
        assertEquals(55, Rule555.distance(-500))
        assertEquals(null, Rule555.distance(600))
    }
}

class DashTrackerTest {
    @Test fun thirdDashGivesPenaltyAndResets() {
        assertEquals(DashTracker.Result(1, 0), DashTracker.registerDash(0))
        assertEquals(DashTracker.Result(2, 0), DashTracker.registerDash(1))
        assertEquals(DashTracker.Result(0, -120), DashTracker.registerDash(2))
    }
}

class RospisEngineTest {
    @Test fun splitIsHalfRoundedTo5() {
        assertEquals(60, RospisEngine.splitAmount(120))
        assertEquals(65, RospisEngine.splitAmount(125))
        assertEquals(55, RospisEngine.splitAmount(110))
        assertEquals(55, RospisEngine.splitAmount(105))
    }

    @Test fun limit() {
        assertTrue(RospisEngine.canRospis(2))
        assertFalse(RospisEngine.canRospis(3))
    }

    @Test fun recipientsAreTheOtherTwo() {
        assertEquals(listOf(0, 3), RospisEngine.recipients(listOf(0, 2, 3), 2))
    }
}

class BochkaEngineTest {
    private val onBochka = PlayerState(0, score = 880, isOnBochka = true, bochkaAttemptsLeft = 3)

    @Test fun cannotJumpOver880() {
        val r = BochkaEngine.applyPoints(PlayerState(0, score = 800), 150)
        assertEquals(PlayerState(0, score = 880, isOnBochka = true, bochkaAttemptsLeft = 3), r.state)
        assertEquals(listOf(ScoreEvent.BochkaEntered(burned = 70)), r.events)
    }

    @Test fun exactly880AlsoBochka() {
        assertTrue(BochkaEngine.applyPoints(PlayerState(0, score = 760), 120).state.isOnBochka)
    }

    @Test fun lessThan120Burns() {
        val r = BochkaEngine.applyPoints(onBochka, 100)
        assertEquals(onBochka.copy(bochkaAttemptsLeft = 2), r.state)
    }

    @Test fun breakWith120() {
        val r = BochkaEngine.applyPoints(onBochka, 120)
        assertEquals(PlayerState(0, score = 1000), r.state)
    }

    @Test fun negativeDropsFromBochka() {
        val r = BochkaEngine.applyPoints(onBochka, -120)
        assertEquals(PlayerState(0, score = 760), r.state)
    }

    @Test fun noAttemptsLeftGivesPenalty() {
        val r = BochkaEngine.resolve(onBochka.copy(bochkaAttemptsLeft = 0))
        assertEquals(PlayerState(0, score = 760), r.state)
        assertEquals(listOf(ScoreEvent.BochkaFailed(-120)), r.events)
    }

    @Test fun penaltyKnocksOffBochka() {
        val r = BochkaEngine.resolve(onBochka.copy(score = 760))
        assertFalse(r.state.isOnBochka)
        assertEquals(listOf(ScoreEvent.BochkaLeft), r.events)
    }
}

class RoundScoreCalculatorTest {
    @Test fun autoLastPlayer() {
        assertEquals(37, RoundScoreCalculator.autoCardPoints(listOf(50, 33)))
    }

    @Test fun takerGetsExactlyTheBid() {
        assertEquals(120, RoundScoreCalculator.takerScore(total = 135, bid = 120))
        assertEquals(120, RoundScoreCalculator.takerScore(total = 120, bid = 120))
        assertEquals(-120, RoundScoreCalculator.takerScore(total = 115, bid = 120))
    }

    @Test fun defenderRoundedToNearest5() {
        assertEquals(35, RoundScoreCalculator.defenderScore(33))
        assertEquals(20, RoundScoreCalculator.defenderScore(22))
        assertEquals(0, RoundScoreCalculator.defenderScore(2))
    }

    @Test fun totalIncludesMarriages() {
        val settings = GameSettings()
        val entry = RoundEntry(0, cardPoints = 40, marriages = listOf(Suit.HEARTS, Suit.SPADES), hasAceMarriage = true)
        assertEquals(40 + 100 + 40 + 200, RoundScoreCalculator.total(entry, settings))
    }

    @Test fun customMarriageValues() {
        val settings = GameSettings(marriageValues = GameSettings.DEFAULT_MARRIAGES + (Suit.SPADES to 50))
        assertEquals(50, MarriageCalculator.points(listOf(Suit.SPADES), false, settings))
    }
}

class CardPickValidatorTest {
    @Test fun points() {
        assertEquals(11 + 11 + 10 + 4, CardPickValidator.pointsOf(mapOf(Rank.ACE to 2, Rank.TEN to 1, Rank.KING to 1, Rank.NINE to 3)))
    }

    @Test fun maxFourPerRankAcrossPlayers() {
        val hands = listOf(mapOf(Rank.ACE to 3), mapOf(Rank.ACE to 1))
        assertEquals(0, CardPickValidator.remaining(Rank.ACE, hands))
        assertFalse(CardPickValidator.canAdd(Rank.ACE, hands))
        assertTrue(CardPickValidator.canAdd(Rank.TEN, hands))
        assertEquals(listOf(Rank.ACE), CardPickValidator.exceededRanks(hands + listOf(mapOf(Rank.ACE to 1))))
    }

    @Test fun addRemove() {
        val cards = CardPickValidator.add(CardPickValidator.add(emptyMap(), Rank.ACE), Rank.ACE)
        assertEquals(mapOf(Rank.ACE to 2), cards)
        assertEquals(emptyMap<Rank, Int>(), CardPickValidator.remove(CardPickValidator.remove(cards, Rank.ACE), Rank.ACE))
    }

    @Test fun prikupMaxThreeCards() {
        assertTrue(PrikupHandler.canAddCard(mapOf(Rank.ACE to 2), Rank.TEN))
        assertFalse(PrikupHandler.canAddCard(mapOf(Rank.ACE to 2, Rank.TEN to 1), Rank.NINE))
    }
}
