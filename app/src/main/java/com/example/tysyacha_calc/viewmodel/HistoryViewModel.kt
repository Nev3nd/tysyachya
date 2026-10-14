package com.example.tysyacha_calc.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tysyacha_calc.data.model.GameTimeline
import com.example.tysyacha_calc.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = true,
    val timeline: GameTimeline? = null,
)

class HistoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GameRepository,
) : ViewModel() {

    private val gameId: Long = checkNotNull(savedStateHandle[GAME_ID_ARG])

    val uiState: StateFlow<HistoryUiState> = repository.observeGame(gameId)
        .map { HistoryUiState(isLoading = false, timeline = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    /** Вернуться к кону [roundNumber]: состояние до него, сам кон — в форме ввода. */
    fun returnToRound(roundNumber: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.moveTo(gameId, roundNumber - 1)
            onDone()
        }
    }
}
