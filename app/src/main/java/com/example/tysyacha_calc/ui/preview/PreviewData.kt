package com.example.tysyacha_calc.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tysyacha_calc.data.model.Bid
import com.example.tysyacha_calc.data.model.CutRitualResult
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.GameSetup
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.GameSummary
import com.example.tysyacha_calc.data.model.GameTimeline
import com.example.tysyacha_calc.data.model.PlayerState
import com.example.tysyacha_calc.data.model.PrikupShare
import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.RoundEntry
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.domain.GameEngine
import com.example.tysyacha_calc.domain.RoundInput
import com.example.tysyacha_calc.domain.RoundOutcome
import com.example.tysyacha_calc.ui.theme.TysyachacalcTheme
import com.example.tysyacha_calc.viewmodel.CalculatorActions
import com.example.tysyacha_calc.viewmodel.CalculatorStateBuilder
import com.example.tysyacha_calc.viewmodel.CalculatorUiState
import com.example.tysyacha_calc.viewmodel.HomeUiState
import com.example.tysyacha_calc.viewmodel.RoundDraft
import com.example.tysyacha_calc.viewmodel.SettingsActions

/** Превью в светлой и тёмной теме одновременно. */
@Preview(name = "Светлая", showBackground = true, locale = "ru")
@Preview(name = "Тёмная", showBackground = true, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews

/** Целый экран телефона в обеих темах. */
@Preview(name = "Светлая", showSystemUi = true, locale = "ru", device = "spec:width=411dp,height=891dp")
@Preview(
    name = "Тёмная", showSystemUi = true, locale = "ru", device = "spec:width=411dp,height=891dp",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class ScreenPreviews

/** Тема приложения + фон + отступ — для превью отдельных компонентов. */
@Composable
fun PreviewBox(content: @Composable () -> Unit) {
    TysyachacalcTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(16.dp)) { content() }
        }
    }
}

/** Пустые действия: в превью кнопки ничего не делают. */
object NoCalculatorActions : CalculatorActions
object NoSettingsActions : SettingsActions

/**
 * Тестовые партии для превью. Считаются настоящим [GameEngine],
 * поэтому превью показывают реальный подсчёт, а не нарисованные цифры.
 */
object PreviewData {

    private fun GameState.play(input: RoundInput): GameState =
        (GameEngine.playRound(this, input) as RoundOutcome.Success).state

    /** Три игрока, три кона: обычный, роспись, кон с болтом. */
    val game3: GameState by lazy {
        GameEngine.newGame(GameSettings(), listOf("Игорь", "Аля", "Дима"))
            .play(
                RoundInput(
                    bid = Bid(1, 120),
                    entries = listOf(
                        RoundEntry(0, 33),
                        RoundEntry(1, 66, marriages = listOf(Suit.HEARTS), isAuto = true),
                        RoundEntry(2, 21, cards = mapOf(Rank.ACE to 1, Rank.TEN to 1)),
                    ),
                    cutRitual = CutRitualResult(ninesCount = 1),
                )
            )
            .play(RoundInput(bid = Bid(2, 110), isRospis = true, cutRitual = CutRitualResult()))
            .play(
                RoundInput(
                    bid = Bid(0, 100),
                    entries = listOf(RoundEntry(0, 100, isAuto = true), RoundEntry(1, 20), RoundEntry(2, 0)),
                    cutRitual = CutRitualResult(),
                )
            )
    }

    /** Четыре игрока, один кон: раздающий записал прикуп. */
    val game4: GameState by lazy {
        GameEngine.newGame(GameSettings(playerCount = 4), listOf("Игорь", "Аля", "Дима", "Лена"))
            .play(
                RoundInput(
                    bid = Bid(1, 100),
                    entries = listOf(RoundEntry(1, 85, isAuto = true), RoundEntry(2, 35), RoundEntry(3, 0)),
                    prikup = PrikupShare(0, 23),
                    cutRitual = CutRitualResult(),
                )
            )
    }

    /** Аля стоит на бочке и пробивает её — партия окончена. */
    val finished: GameState by lazy {
        val g = GameEngine.newGame(GameSettings(), listOf("Игорь", "Аля", "Дима"))
        g.copy(
            playerStates = listOf(
                PlayerState(0, 640, dashCount = 1),
                PlayerState(1, 880, isOnBochka = true, bochkaAttemptsLeft = 2),
                PlayerState(2, 415, dashCount = 2),
            )
        ).play(
            RoundInput(
                bid = Bid(1, 140),
                entries = listOf(RoundEntry(1, 120, marriages = listOf(Suit.SPADES), isAuto = true), RoundEntry(2, 0), RoundEntry(0, 0)),
                cutRitual = CutRitualResult(),
                isLong = true,
            )
        )
    }

    fun timeline(state: GameState, position: Int = state.rounds.size): GameTimeline {
        // Как в GameRepository: на последней позиции пересчёт не нужен.
        val atPosition = if (position == state.rounds.size) state
        else GameEngine.replay(state.toSession().copy(rounds = state.rounds.take(position)))
        return GameTimeline(state = atPosition, full = state, position = position)
    }

    /** Калькулятор: торги ещё идут, прикуп никто не взял. */
    val calculatorNoTaker: CalculatorUiState by lazy {
        val t = timeline(game3)
        CalculatorStateBuilder.build(t, RoundDraft.initial(t.state))
    }

    /** Калькулятор: Аля взяла на 130, у Игоря очки картами, у Димы марьяж. */
    val calculatorFilled: CalculatorUiState by lazy {
        val t = timeline(game3)
        val draft = RoundDraft.initial(t.state).copy(takerId = 1, bid = 130, nines = 2)
            .updateHand(1) { it.copy(marriages = listOf(Suit.HEARTS)) }
            .updateHand(0) { it.copy(cards = mapOf(Rank.ACE to 2, Rank.KING to 1)) }
            .updateHand(2) { it.copy(pointsText = "17", marriages = listOf(Suit.SPADES)) }
        CalculatorStateBuilder.build(t, draft)
    }

    /** Калькулятор после возврата ко 2-му кону из 3. */
    val calculatorTimeline: CalculatorUiState by lazy {
        val t = timeline(game3, position = 1)
        CalculatorStateBuilder.build(t, RoundDraft.fromRound(game3.rounds[1]))
    }

    /** Калькулятор на четверых: раздающий в прикупе. */
    val calculator4: CalculatorUiState by lazy {
        val t = timeline(game4)
        val draft = RoundDraft.initial(t.state).copy(takerId = 3, prikupCards = mapOf(Rank.ACE to 1, Rank.QUEEN to 1))
            .updateHand(0) { it.copy(pointsText = "40") }
        CalculatorStateBuilder.build(t, draft)
    }

    val calculatorFinished: CalculatorUiState by lazy { CalculatorUiState(timeline = timeline(finished)) }

    fun summary(state: GameState, id: Long) = GameSummary(
        id = id,
        createdAt = 1_760_000_000_000,
        updatedAt = 1_760_400_000_000,
        players = state.players,
        scores = state.playerStates.map { it.score },
        roundsCount = state.rounds.size,
        winnerId = state.winnerId,
    )

    val home: HomeUiState by lazy {
        HomeUiState(isLoading = false, current = summary(game3, 1), games = listOf(summary(finished, 2), summary(game4, 3)))
    }

    val setup = GameSetup(playerNames = listOf("Игорь", "Аля", "Дима", "Лена"))
}
