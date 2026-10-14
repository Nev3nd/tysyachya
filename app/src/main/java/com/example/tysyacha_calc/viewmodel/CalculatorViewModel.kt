package com.example.tysyacha_calc.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tysyacha_calc.data.local.AppJson
import com.example.tysyacha_calc.data.model.GameTimeline
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.domain.CardPickValidator
import com.example.tysyacha_calc.domain.GameEngine
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.domain.PrikupHandler
import com.example.tysyacha_calc.domain.RoundOutcome
import com.example.tysyacha_calc.domain.RoundScoreCalculator
import com.example.tysyacha_calc.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: GameRepository,
) : ViewModel(), CalculatorActions {

    val gameId: Long = checkNotNull(savedStateHandle[GAME_ID_ARG])

    private val timeline: StateFlow<GameTimeline?> = repository.observeGame(gameId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val draft = MutableStateFlow(
        savedStateHandle.get<String>(DRAFT_KEY)?.let {
            runCatching { AppJson.decodeFromString(RoundDraft.serializer(), it) }.getOrNull()
        }
    )

    val uiState: StateFlow<CalculatorUiState> =
        combine(timeline.filterNotNull(), draft.filterNotNull()) { t, d ->
            if (d.roundNumber == t.state.nextRoundNumber) CalculatorStateBuilder.build(t, d)
            else CalculatorUiState(timeline = t)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalculatorUiState())

    private val _events = Channel<CalculatorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Сменилась позиция (записали кон, шагнули назад/вперёд) — подставляем кон в форму:
        // сохранённый, если он уже был, иначе чистый.
        viewModelScope.launch {
            timeline.filterNotNull().collect { t ->
                if (draft.value?.roundNumber != t.state.nextRoundNumber) {
                    setDraft(t.storedNext?.let(RoundDraft::fromRound) ?: RoundDraft.initial(t.state))
                }
            }
        }
    }

    private val game get() = timeline.value?.state

    // --- Торги ---

    override fun setTaker(playerId: Int) = edit { d ->
        if (d.takerId == playerId) d
        // Взявшему прикуп очки не вводятся — сбрасываем, марьяжи оставляем.
        else d.copy(takerId = playerId).updateHand(playerId) { it.copy(pointsText = "0", cards = emptyMap()) }
    }

    override fun changeBid(delta: Int) = edit {
        val max = game?.let { g -> RoundScoreCalculator.maxBid(g.settings) } ?: Int.MAX_VALUE
        it.copy(bid = (it.bid + delta).coerceIn(GameRules.MIN_BID, max))
    }

    override fun setBid(amount: Int) = edit { it.copy(bid = amount.coerceAtLeast(GameRules.MIN_BID)) }

    override fun toggleRospis() = edit { d ->
        val taker = d.takerId
        val canRospis = taker != null && (game?.rospisLeftFor(taker) ?: 0) > 0
        d.copy(isRospis = !d.isRospis && canRospis, isLong = false)
    }

    override fun toggleLong() = edit { it.copy(isLong = !it.isLong) }

    // --- Сдвиг колоды ---

    override fun changeNines(delta: Int) = edit { it.copy(nines = (it.nines + delta).coerceAtLeast(0)) }

    override fun changeJacks(delta: Int) = edit { it.copy(jacks = (it.jacks + delta).coerceAtLeast(0)) }

    // --- Очки игроков ---

    /** Число очков картами; больше, чем осталось от 120, ввести нельзя. */
    override fun setPoints(playerId: Int, text: String) = edit { d ->
        val g = game ?: return@edit d
        val digits = text.filter(Char::isDigit).trimStart('0').take(3)
        val max = d.maxPointsFor(playerId, g)
        val value = digits.toIntOrNull()?.coerceAtMost(max)
        d.updateHand(playerId) { it.copy(pointsText = value?.toString() ?: "", cards = emptyMap()) }
    }

    override fun addCard(playerId: Int, rank: Rank) = edit { d ->
        val g = game ?: return@edit d
        val allCards = d.defenders(g).map { d.hand(it).cards }
        val hand = d.hand(playerId)
        val fits = CardPickValidator.pointsOf(hand.cards) + rank.points <= d.maxPointsFor(playerId, g)
        if (!CardPickValidator.canAdd(rank, allCards) || !fits) d
        else d.updateHand(playerId) { it.copy(cards = CardPickValidator.add(it.cards, rank), pointsText = "0") }
    }

    override fun removeCard(playerId: Int, rank: Rank) = edit { d ->
        d.updateHand(playerId) { it.copy(cards = CardPickValidator.remove(it.cards, rank)) }
    }

    override fun toggleMarriage(playerId: Int, suit: Suit) = edit { d ->
        val takenByOther = d.hands.any { (id, h) -> id != playerId && suit in h.marriages }
        d.updateHand(playerId) { h ->
            when {
                suit in h.marriages -> h.copy(marriages = h.marriages - suit)
                takenByOther -> h
                else -> h.copy(marriages = (h.marriages + suit).sortedBy { it.ordinal })
            }
        }
    }

    override fun toggleAceMarriage(playerId: Int) = edit { d ->
        val takenByOther = d.hands.any { (id, h) -> id != playerId && h.hasAceMarriage }
        d.updateHand(playerId) { h -> h.copy(hasAceMarriage = !h.hasAceMarriage && !takenByOther) }
    }

    // --- Прикуп (4 игрока) ---

    override fun setPrikupPoints(text: String) = edit {
        val value = text.filter(Char::isDigit).trimStart('0').take(2).toIntOrNull()?.coerceAtMost(PrikupHandler.maxPoints)
        it.copy(prikupText = value?.toString() ?: "", prikupCards = emptyMap())
    }

    override fun addPrikupCard(rank: Rank) = edit { d ->
        if (!PrikupHandler.canAddCard(d.prikupCards, rank)) d
        else d.copy(prikupCards = CardPickValidator.add(d.prikupCards, rank), prikupText = "0")
    }

    override fun removePrikupCard(rank: Rank) = edit { it.copy(prikupCards = CardPickValidator.remove(it.prikupCards, rank)) }

    // --- Кон и история ---

    override fun resetRound() {
        game?.let { setDraft(RoundDraft.initial(it)) }
    }

    override fun submit() {
        val t = timeline.value ?: return
        val input = draft.value?.toInput(t.state) ?: return
        val outcome = GameEngine.playRound(t.state, input) as? RoundOutcome.Success ?: return
        val round = outcome.result.round
        viewModelScope.launch {
            if (t.storedNext == round) {
                // Ничего не поменяли — просто шагаем вперёд, будущие коны сохраняются.
                repository.moveTo(gameId, t.position + 1)
                _events.send(CalculatorEvent.RoundSaved(round.roundNumber, replacedFuture = 0))
            } else {
                repository.saveRound(gameId, outcome.state)
                _events.send(CalculatorEvent.RoundSaved(round.roundNumber, replacedFuture = t.totalRounds - t.position))
            }
        }
    }

    /** Шаг назад: последний кон не удаляется, а возвращается в форму. */
    override fun stepBack() = moveBy(-1)

    override fun stepForward() = moveBy(1)

    override fun moveToLatest() {
        val t = timeline.value ?: return
        viewModelScope.launch { repository.moveTo(gameId, t.totalRounds) }
    }

    private fun moveBy(delta: Int) {
        val t = timeline.value ?: return
        val target = t.position + delta
        if (target !in 0..t.totalRounds) return
        viewModelScope.launch { repository.moveTo(gameId, target) }
    }

    private fun edit(transform: (RoundDraft) -> RoundDraft) {
        draft.value?.let { setDraft(transform(it)) }
    }

    private fun setDraft(value: RoundDraft) {
        draft.value = value
        savedStateHandle[DRAFT_KEY] = AppJson.encodeToString(RoundDraft.serializer(), value)
    }

    private companion object {
        const val DRAFT_KEY = "round_draft"
    }
}
