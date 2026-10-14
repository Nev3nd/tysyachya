package com.example.tysyacha_calc.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tysyacha_calc.ui.components.ScoreBoard
import com.example.tysyacha_calc.ui.preview.NoCalculatorActions
import com.example.tysyacha_calc.ui.preview.PreviewData
import com.example.tysyacha_calc.ui.preview.ScreenPreviews
import com.example.tysyacha_calc.ui.theme.TysyachacalcTheme
import com.example.tysyacha_calc.viewmodel.AppViewModelProvider
import com.example.tysyacha_calc.viewmodel.CalculatorActions
import com.example.tysyacha_calc.viewmodel.CalculatorEvent
import com.example.tysyacha_calc.viewmodel.CalculatorUiState
import com.example.tysyacha_calc.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    onOpenHistory: (Long) -> Unit,
    onNewGame: () -> Unit,
    viewModel: CalculatorViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CalculatorEvent.RoundSaved -> {
                    val text = if (event.replacedFuture > 1) {
                        "Кон ${event.roundNumber} записан, дальнейшие коны заменены"
                    } else "Кон ${event.roundNumber} записан"
                    val result = snackbar.showSnackbar(text, actionLabel = "Назад", duration = SnackbarDuration.Short)
                    if (result == SnackbarResult.ActionPerformed) viewModel.stepBack()
                }
            }
        }
    }

    // За столом телефон лежит долго — экран не гасим.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    CalculatorContent(
        state = state,
        snackbar = snackbar,
        onBack = onBack,
        onOpenHistory = { onOpenHistory(viewModel.gameId) },
        onNewGame = onNewGame,
        actions = viewModel,
    )
}

/** Вёрстка калькулятора без ViewModel: состояние и действия приходят снаружи (так её видно в превью). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorContent(
    state: CalculatorUiState,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewGame: () -> Unit,
    actions: CalculatorActions,
) {
    val timeline = state.timeline
    val game = state.game
    val draft = state.draft
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if (game != null) {
                        Column {
                            Text(if (game.isFinished) "Партия окончена" else "Кон ${game.nextRoundNumber}")
                            if (!game.isFinished) {
                                Text(
                                    "раздаёт ${game.player(game.dealerId).name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "На главную")
                    }
                },
                actions = {
                    IconButton(onClick = actions::stepBack, enabled = timeline?.canGoBack == true) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Предыдущий кон")
                    }
                    IconButton(onClick = actions::stepForward, enabled = timeline?.hasFuture == true) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Следующий кон")
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "История")
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Ещё")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Очистить ввод") },
                            enabled = game?.isFinished == false,
                            onClick = { menuOpen = false; actions.resetRound() },
                        )
                        DropdownMenuItem(
                            text = { Text("Новая партия") },
                            onClick = { menuOpen = false; onNewGame() },
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            if (game != null && !game.isFinished && draft != null) {
                SubmitBar(state, onSubmit = actions::submit)
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (timeline == null || game == null || (draft == null && !game.isFinished)) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "board") { ScoreBoard(game, preview = state.preview) }

            if (timeline.hasFuture) {
                item(key = "timeline") {
                    TimelineBanner(timeline, onForward = actions::stepForward, onLatest = actions::moveToLatest)
                }
            }

            if (game.isFinished || draft == null) {
                item(key = "winner") {
                    WinnerCard(game, onHistory = onOpenHistory, onNewGame = onNewGame, onUndo = actions::stepBack)
                }
                return@LazyColumn
            }

            if (game.settings.cutRitualEnabled) {
                item(key = "cut") {
                    CutRitualSection(game, draft, onNines = actions::changeNines, onJacks = actions::changeJacks)
                }
            }
            item(key = "bid") {
                BiddingSection(
                    game = game,
                    draft = draft,
                    onTakerChange = actions::setTaker,
                    onBidStep = actions::changeBid,
                    onBidSet = actions::setBid,
                    onToggleRospis = actions::toggleRospis,
                )
            }
            if (draft.takerId == null) return@LazyColumn

            if (draft.isRospis) {
                item(key = "rospis") { RospisCard(game, draft.takerId, draft.bid) }
            } else {
                items(state.hands, key = { "hand-${it.playerId}" }) { hand ->
                    if (hand.isTaker) {
                        TakerCard(
                            hand = hand,
                            settings = game.settings,
                            bid = draft.bid,
                            isLong = draft.isLong,
                            onToggleMarriage = { actions.toggleMarriage(hand.playerId, it) },
                            onToggleAce = { actions.toggleAceMarriage(hand.playerId) },
                            onToggleLong = actions::toggleLong,
                            modifier = Modifier.animateItem(),
                        )
                    } else {
                        DefenderCard(
                            hand = hand,
                            settings = game.settings,
                            onPointsChange = { actions.setPoints(hand.playerId, it) },
                            onAddCard = { actions.addCard(hand.playerId, it) },
                            onRemoveCard = { actions.removeCard(hand.playerId, it) },
                            onToggleMarriage = { actions.toggleMarriage(hand.playerId, it) },
                            onToggleAce = { actions.toggleAceMarriage(hand.playerId) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
            state.prikup?.let { prikup ->
                item(key = "prikup") {
                    PrikupSection(
                        prikup = prikup,
                        onPointsChange = actions::setPrikupPoints,
                        onAddCard = actions::addPrikupCard,
                        onRemoveCard = actions::removePrikupCard,
                    )
                }
            }
            item(key = "preview") { RoundPreviewSection(state) }
        }
    }
}

@Composable
private fun SubmitBar(state: CalculatorUiState, onSubmit: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val focus = LocalFocusManager.current
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 3.dp) {
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                focus.clearFocus()
                onSubmit()
            },
            enabled = state.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            Text(
                if (state.draft?.takerId == null) "Выбери, кто взял прикуп"
                else "Записать кон ${state.game?.nextRoundNumber ?: ""}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun CalculatorNoTakerPreview() {
    TysyachacalcTheme {
        CalculatorContent(PreviewData.calculatorNoTaker, SnackbarHostState(), {}, {}, {}, NoCalculatorActions)
    }
}

@ScreenPreviews
@Composable
private fun CalculatorFilledPreview() {
    TysyachacalcTheme {
        CalculatorContent(PreviewData.calculatorFilled, SnackbarHostState(), {}, {}, {}, NoCalculatorActions)
    }
}

@ScreenPreviews
@Composable
private fun CalculatorTimelinePreview() {
    TysyachacalcTheme {
        CalculatorContent(PreviewData.calculatorTimeline, SnackbarHostState(), {}, {}, {}, NoCalculatorActions)
    }
}

@ScreenPreviews
@Composable
private fun CalculatorFourPlayersPreview() {
    TysyachacalcTheme {
        CalculatorContent(PreviewData.calculator4, SnackbarHostState(), {}, {}, {}, NoCalculatorActions)
    }
}

@ScreenPreviews
@Composable
private fun CalculatorFinishedPreview() {
    TysyachacalcTheme {
        CalculatorContent(PreviewData.calculatorFinished, SnackbarHostState(), {}, {}, {}, NoCalculatorActions)
    }
}
