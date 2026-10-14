package com.example.tysyacha_calc.viewmodel

import com.example.tysyacha_calc.data.model.GameTimeline
import com.example.tysyacha_calc.data.model.PrikupShare
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.RoundEntry
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.domain.CardPickValidator
import com.example.tysyacha_calc.domain.GameEngine
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.domain.MarriageCalculator
import com.example.tysyacha_calc.domain.PrikupHandler
import com.example.tysyacha_calc.domain.RoundOutcome
import com.example.tysyacha_calc.domain.RoundScoreCalculator

/** Собирает [CalculatorUiState] из партии и черновика. Чистая функция — без Android. */
object CalculatorStateBuilder {

    fun build(timeline: GameTimeline, draft: RoundDraft): CalculatorUiState {
        val game = timeline.state
        val nameOf = { id: Int -> game.player(id).name }
        val settings = game.settings
        val takerId = draft.takerId
            ?: return CalculatorUiState(timeline = timeline, draft = draft)

        val marriagesSoFar = game.activePlayerIds.map { id ->
            val h = draft.hand(id)
            RoundEntry(id, 0, h.marriages, h.hasAceMarriage)
        }
        val allCards = draft.defenders(game).map { draft.hand(it).cards }

        val order = listOf(takerId) + draft.defenders(game)
        val hands = order.map { id ->
            val h = draft.hand(id)
            val isTaker = id == takerId
            val locked = draft.isLong && !isTaker
            val cardPoints = if (isTaker) draft.takerCardPoints(game) else draft.defenderPoints(id)
            val max = draft.maxPointsFor(id, game)
            val pickedPoints = CardPickValidator.pointsOf(h.cards)
            val marriagePoints = if (locked) 0 else MarriageCalculator.points(h.marriages, h.hasAceMarriage, settings)
            val total = cardPoints + marriagePoints
            HandUi(
                playerId = id,
                name = nameOf(id),
                isTaker = isTaker,
                draft = h,
                cardPoints = cardPoints,
                maxPoints = max,
                remaining = Rank.entries.associateWith { rank ->
                    if (pickedPoints + rank.points > max) 0 else CardPickValidator.remaining(rank, allCards)
                },
                blockedSuits = Suit.entries.filter { s ->
                    MarriageCalculator.ownerOf(s, marriagesSoFar).let { it != null && it != id }
                }.toSet(),
                aceBlocked = MarriageCalculator.aceMarriageOwner(marriagesSoFar).let { it != null && it != id },
                marriagePoints = marriagePoints,
                total = total,
                roundScore = if (isTaker) RoundScoreCalculator.takerScore(total, draft.bid)
                else RoundScoreCalculator.defenderScore(total),
                isLocked = locked,
            )
        }

        val prikup = game.prikupPlayerId?.let { id ->
            PrikupUi(
                playerId = id,
                name = nameOf(id),
                text = draft.prikupText,
                cards = draft.prikupCards,
                points = draft.prikupPoints,
                roundScore = PrikupHandler.score(PrikupShare(id, draft.prikupPoints)),
            )
        }

        val warnings = buildList {
            val taker = draft.hand(takerId)
            if (!draft.isRospis && draft.bid > GameRules.MAX_BID_WITHOUT_MARRIAGE &&
                taker.marriages.isEmpty() && !taker.hasAceMarriage
            ) {
                add("Больше ${GameRules.MAX_BID_WITHOUT_MARRIAGE} ставят только с марьяжем — отметь его у ${nameOf(takerId)}")
            }
            if (timeline.hasFuture) {
                val from = game.nextRoundNumber
                add("Запись заменит сохранённые коны $from–${timeline.totalRounds}")
            }
        }

        val outcome = draft.toInput(game)?.let { GameEngine.playRound(game, it) }
        return CalculatorUiState(
            timeline = timeline,
            draft = draft,
            hands = hands,
            prikup = prikup,
            preview = (outcome as? RoundOutcome.Success)?.result?.players,
            errors = (outcome as? RoundOutcome.Invalid)?.errors.orEmpty().map { it.message(nameOf) },
            warnings = warnings,
        )
    }
}
