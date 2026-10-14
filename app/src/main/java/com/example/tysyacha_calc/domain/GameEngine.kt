package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.CutRitualResult
import com.example.tysyacha_calc.data.model.GameRound
import com.example.tysyacha_calc.data.model.GameSession
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.Player
import com.example.tysyacha_calc.data.model.PlayerRoundResult
import com.example.tysyacha_calc.data.model.PlayerState
import com.example.tysyacha_calc.data.model.PrikupShare
import com.example.tysyacha_calc.data.model.RoundResult
import com.example.tysyacha_calc.data.model.ScoreEvent

/**
 * Применяет кон к состоянию партии. Порядок для каждого игрока:
 * 1. очки за кон (взявший прикуп ±ставка / остальные — карты + марьяжи до 5 / половина росписи / прикуп)
 *    через «стену» бочки; не бравший прикуп с 0 получает болт;
 * 2. болт: +1 к счётчику, на 3-м −120;
 * 3. штрафы сдвига: −120 сдвигавшему за каждые 3 девятки, −120 дилеру за каждые 3 валета;
 * 4. бочка: слетел с 880 — снята; кончились попытки — −120;
 * 5. минус выключен — счёт не ниже 0;
 * 6. правило 555 (если включено);
 * 7. ≥1000 — победа.
 */
object GameEngine {

    fun newGame(settings: GameSettings, names: List<String>): GameState {
        require(names.size == settings.playerCount) { "Нужно ${settings.playerCount} имени, а не ${names.size}" }
        val players = names.mapIndexed { i, name -> Player(i, name.trim().ifEmpty { "Игрок ${i + 1}" }) }
        return initialState(settings, players)
    }

    fun initialState(settings: GameSettings, players: List<Player>): GameState {
        GameSession(settings, players) // проверка инвариантов
        return GameState(settings, players, players.map { PlayerState(it.id) })
    }

    /** Пересчитать партию с нуля из сохранённых конов. */
    fun replay(session: GameSession): GameState =
        session.rounds.fold(initialState(session.settings, session.players)) { state, round ->
            check(!state.isFinished) { "Кон ${round.roundNumber} после окончания партии" }
            check(round.roundNumber == state.nextRoundNumber) { "Ожидался кон ${state.nextRoundNumber}, а не ${round.roundNumber}" }
            check(round.dealerId == state.dealerId) { "В кону ${round.roundNumber} неверный раздающий" }
            applyRound(state, round).first
        }

    fun undoLastRound(state: GameState): GameState =
        replay(state.toSession().copy(rounds = state.rounds.dropLast(1)))

    fun playRound(state: GameState, input: RoundInput): RoundOutcome {
        val errors = validate(state, input)
        if (errors.isNotEmpty()) return RoundOutcome.Invalid(errors)
        val (newState, result) = applyRound(state, buildRound(state, input))
        return RoundOutcome.Success(newState, result)
    }

    fun validate(state: GameState, input: RoundInput): List<RoundError> {
        if (state.isFinished) return listOf(RoundError.GameFinished)

        val errors = mutableListOf<RoundError>()
        val active = state.activePlayerIds

        input.cutRitual?.let { cut ->
            if (cut.ninesCount < 0 || cut.jacksCount < 0) errors += RoundError.InvalidCutCount
            if (!state.settings.cutRitualEnabled && !cut.isEmpty) errors += RoundError.CutRitualDisabled
        }

        val bid = input.bid
        if (bid.takerId !in active) errors += RoundError.TakerNotActive(bid.takerId)
        if (bid.amount < GameRules.MIN_BID) errors += RoundError.BidTooLow(bid.amount)
        if (!ScoreRoundingUtil.isMultipleOf5(bid.amount)) errors += RoundError.BidNotMultipleOf5(bid.amount)

        val prikup = input.prikup
        if (prikup != null) {
            if (state.prikupPlayerId == null) errors += RoundError.PrikupNotAllowed
            else if (prikup.playerId != state.prikupPlayerId || !PrikupHandler.isValid(prikup)) {
                errors += RoundError.InvalidPrikup
            }
        }

        input.entries.map { it.playerId }.filter { it !in active }.distinct()
            .forEach { errors += RoundError.UnexpectedEntry(it) }

        if (input.isRospis) {
            if (!RospisEngine.canRospis(state.rospisUsedBy(bid.takerId))) errors += RoundError.RospisLimitReached
            if (input.entries.isNotEmpty() || input.isLong) errors += RoundError.RospisWithEntries
            return errors
        }

        for (id in active) {
            when (input.entries.count { it.playerId == id }) {
                0 -> errors += RoundError.MissingEntry(id)
                1 -> Unit
                else -> errors += RoundError.UnexpectedEntry(id)
            }
        }
        val entries = input.entries.filter { it.playerId in active }
        for (entry in entries) {
            if (entry.cardPoints !in 0..GameRules.CARD_POINTS_TOTAL) {
                errors += RoundError.CardPointsOutOfRange(entry.playerId, entry.cardPoints)
            }
            if (entry.cards.isNotEmpty() && CardPickValidator.pointsOf(entry.cards) != entry.cardPoints) {
                errors += RoundError.CardsMismatch(entry.playerId)
            }
        }
        if (entries.size == active.size) {
            val sum = entries.sumOf { it.cardPoints }
            if (sum != GameRules.CARD_POINTS_TOTAL) errors += RoundError.CardPointsSum(sum)
        }
        CardPickValidator.exceededRanks(entries.map { it.cards })
            .forEach { errors += RoundError.CardLimitExceeded(it) }
        MarriageCalculator.duplicatedSuits(entries).forEach { errors += RoundError.DuplicateMarriage(it) }
        if (MarriageCalculator.isAceMarriageDuplicated(entries)) errors += RoundError.DuplicateAceMarriage
        if (input.isLong) {
            val longOk = entries.all { e ->
                if (e.playerId == bid.takerId) e.cardPoints == GameRules.CARD_POINTS_TOTAL
                else e.cardPoints == 0 && e.marriages.isEmpty() && !e.hasAceMarriage
            }
            if (!longOk) errors += RoundError.InvalidLong
        }
        return errors
    }

    private fun buildRound(state: GameState, input: RoundInput): GameRound = GameRound(
        roundNumber = state.nextRoundNumber,
        dealerId = state.dealerId,
        cutRitualResult = if (state.settings.cutRitualEnabled) input.cutRitual ?: CutRitualResult() else null,
        bid = input.bid,
        entries = if (input.isRospis) emptyList()
        else state.activePlayerIds.map { id -> input.entries.first { it.playerId == id } },
        prikup = state.prikupPlayerId?.let { input.prikup ?: PrikupShare(it, 0) },
        isRospis = input.isRospis,
        isLong = input.isLong,
    )

    private fun applyRound(state: GameState, round: GameRound): Pair<GameState, RoundResult> {
        val playerResults = state.playerStates.map { applyToPlayer(state, round, it) }
        val newStates = playerResults.map { it.first }
        val winnerId = newStates
            .filter { it.score >= GameRules.WIN_SCORE }
            .maxByOrNull { it.score }
            ?.playerId
        val result = RoundResult(round, playerResults.map { it.second })
        val newState = state.copy(
            playerStates = newStates,
            rounds = state.rounds + round,
            results = state.results + result,
            winnerId = winnerId,
        )
        return newState to result
    }

    private fun applyToPlayer(
        game: GameState,
        round: GameRound,
        before: PlayerState,
    ): Pair<PlayerState, PlayerRoundResult> {
        val id = before.playerId
        val settings = game.settings
        val events = mutableListOf<ScoreEvent>()
        val bid = round.bid
        val entry = round.entries.firstOrNull { it.playerId == id }
        val prikup = round.prikup

        // 1. Очки за кон
        var points = 0
        var isDash = false
        when {
            round.isRospis && id == bid.takerId -> events += ScoreEvent.RospisTaker(bid.amount)
            round.isRospis && id in game.activePlayerIds -> {
                points = RospisEngine.splitAmount(bid.amount)
                events += ScoreEvent.RospisBonus(points)
            }
            prikup != null && id == prikup.playerId -> {
                points = PrikupHandler.score(prikup)
                events += ScoreEvent.Prikup(prikup.cardPoints, points)
            }
            entry != null && id == bid.takerId -> {
                val total = RoundScoreCalculator.total(entry, settings)
                points = RoundScoreCalculator.takerScore(total, bid.amount)
                events += if (points > 0) ScoreEvent.TakerMade(total, bid.amount, round.isLong)
                else ScoreEvent.TakerFailed(total, bid.amount)
            }
            entry != null -> {
                val total = RoundScoreCalculator.total(entry, settings)
                points = RoundScoreCalculator.defenderScore(total)
                if (points == 0) isDash = true
                else events += ScoreEvent.Defender(total, points)
            }
        }
        val afterPoints = BochkaEngine.applyPoints(before, points)
        var cur = afterPoints.state

        // 2–3. Штрафы
        var penalties = 0
        val penaltyEvents = mutableListOf<ScoreEvent>()
        if (isDash) {
            val dash = DashTracker.registerDash(cur.dashCount)
            cur = cur.copy(dashCount = dash.dashCount)
            events += ScoreEvent.Dash(if (dash.penalty != 0) GameRules.DASH_LIMIT else dash.dashCount)
            if (dash.penalty != 0) {
                penalties += dash.penalty
                penaltyEvents += ScoreEvent.DashPenalty(dash.penalty)
            }
        }
        events += afterPoints.events
        events += penaltyEvents
        round.cutRitualResult?.let { cut ->
            if (id == game.cutterId && cut.cutterPenalty != 0) {
                penalties += cut.cutterPenalty
                events += ScoreEvent.CutNinesPenalty(cut.ninesCount, cut.cutterPenalty)
            }
            if (id == round.dealerId && cut.dealerPenalty != 0) {
                penalties += cut.dealerPenalty
                events += ScoreEvent.CutJacksPenalty(cut.jacksCount, cut.dealerPenalty)
            }
        }
        cur = cur.copy(score = cur.score + penalties)

        // 4. Бочка
        val resolved = BochkaEngine.resolve(cur)
        cur = resolved.state
        events += resolved.events

        // 5. Без ухода в минус
        if (!settings.allowNegativeScore && cur.score < 0) {
            events += ScoreEvent.ClampedAtZero(-cur.score)
            cur = cur.copy(score = 0)
        }

        // 6. 555
        if (settings.use555Rule && Rule555.isTriggered(cur.score)) {
            events += ScoreEvent.Reset555(cur.score)
            cur = cur.copy(score = 0)
        }

        return cur to PlayerRoundResult(id, before.score, cur.score, events)
    }
}
