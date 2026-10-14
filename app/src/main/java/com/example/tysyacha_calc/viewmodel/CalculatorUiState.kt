package com.example.tysyacha_calc.viewmodel

import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.GameTimeline
import com.example.tysyacha_calc.data.model.PlayerRoundResult
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.Suit

/** Всё, что нужно экрану одного игрока в кону. */
data class HandUi(
    val playerId: Int,
    val name: String,
    val isTaker: Boolean,
    val draft: HandDraft,
    /** Очки картами (у взявшего прикуп — остаток от 120). */
    val cardPoints: Int,
    /** Больше этого картами не набрать — остальные уже забрали своё. */
    val maxPoints: Int,
    /** Сколько карт номинала ещё можно добавить этому игроку. */
    val remaining: Map<Rank, Int>,
    /** Масти, марьяж которых уже объявил кто-то другой. */
    val blockedSuits: Set<Suit>,
    val aceBlocked: Boolean,
    val marriagePoints: Int,
    val total: Int,
    /** Очки в таблицу за кон (без бочки и штрафов). */
    val roundScore: Int,
    /** «Длинная»: у этого игрока ввод заблокирован. */
    val isLocked: Boolean,
)

data class PrikupUi(
    val playerId: Int,
    val name: String,
    val text: String,
    val cards: Map<Rank, Int>,
    val points: Int,
    val roundScore: Int,
)

data class CalculatorUiState(
    val timeline: GameTimeline? = null,
    val draft: RoundDraft? = null,
    val hands: List<HandUi> = emptyList(),
    val prikup: PrikupUi? = null,
    /** Итог кона по игрокам (со всеми правилами), если ввод корректный. */
    val preview: List<PlayerRoundResult>? = null,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    val game: GameState? get() = timeline?.state
    val isLoading: Boolean get() = timeline == null
    val canSubmit: Boolean get() = preview != null
}

sealed interface CalculatorEvent {
    data class RoundSaved(val roundNumber: Int, val replacedFuture: Int) : CalculatorEvent
}
