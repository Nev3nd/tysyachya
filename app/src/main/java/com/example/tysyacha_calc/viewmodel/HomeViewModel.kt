package com.example.tysyacha_calc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tysyacha_calc.data.model.GameSummary
import com.example.tysyacha_calc.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    /** Последняя незаконченная партия — её предлагаем продолжить. */
    val current: GameSummary? = null,
    val games: List<GameSummary> = emptyList(),
)

class HomeViewModel(private val repository: GameRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.observeGames()
        .map { games ->
            val current = games.firstOrNull { !it.isFinished }
            HomeUiState(isLoading = false, current = current, games = games.filter { it.id != current?.id })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun deleteGame(id: Long) {
        viewModelScope.launch { repository.deleteGame(id) }
    }
}
