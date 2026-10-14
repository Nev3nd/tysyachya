package com.example.tysyacha_calc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.GameSetup
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.repository.GameRepository
import com.example.tysyacha_calc.repository.SettingsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Экран новой партии: настройки сразу сохраняются как значения по умолчанию. */
@OptIn(FlowPreview::class)
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val gameRepository: GameRepository,
) : ViewModel(), SettingsActions {

    private val _setup = MutableStateFlow<GameSetup?>(null)
    val setup: StateFlow<GameSetup?> = _setup.asStateFlow()

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()

    init {
        viewModelScope.launch {
            _setup.value = settingsRepository.setup.first()
            _setup.filterNotNull().drop(1).debounce(300).collect { settingsRepository.save(it) }
        }
    }

    private fun update(transform: (GameSetup) -> GameSetup) = _setup.update { it?.let(transform) }

    private fun updateSettings(transform: (GameSettings) -> GameSettings) =
        update { it.copy(settings = transform(it.settings)) }

    override fun setPlayerCount(count: Int) = updateSettings { it.copy(playerCount = count) }

    override fun setName(index: Int, name: String) = update { s ->
        s.copy(playerNames = s.playerNames.toMutableList().also { it[index] = name.take(MAX_NAME) })
    }

    /** Поменять местами игрока [index] с соседом (порядок = порядок мест за столом). */
    override fun movePlayer(index: Int, delta: Int) = update { s ->
        val count = s.settings.playerCount
        val target = index + delta
        if (target !in 0 until count) s
        else s.copy(playerNames = s.playerNames.toMutableList().also {
            val tmp = it[index]; it[index] = it[target]; it[target] = tmp
        })
    }

    override fun setAllowNegative(value: Boolean) = updateSettings { it.copy(allowNegativeScore = value) }
    override fun setCutRitual(value: Boolean) = updateSettings { it.copy(cutRitualEnabled = value) }
    override fun setUse555(value: Boolean) = updateSettings { it.copy(use555Rule = value) }
    override fun setShow555Progress(value: Boolean) = updateSettings { it.copy(show555Progress = value) }

    override fun changeMarriage(suit: Suit, delta: Int) = updateSettings {
        val value = (it.marriageValue(suit) + delta).coerceIn(MARRIAGE_MIN, MARRIAGE_MAX)
        it.copy(marriageValues = it.marriageValues + (suit to value))
    }

    override fun changeAceMarriage(delta: Int) = updateSettings {
        it.copy(aceMarriageValue = (it.aceMarriageValue + delta).coerceIn(MARRIAGE_MIN, MARRIAGE_MAX))
    }

    override fun resetMarriages() = updateSettings {
        it.copy(marriageValues = GameSettings.DEFAULT_MARRIAGES, aceMarriageValue = GameSettings.DEFAULT_ACE_MARRIAGE)
    }

    fun startGame(onCreated: (Long) -> Unit) {
        val setup = _setup.value ?: return
        if (_isStarting.value) return
        _isStarting.value = true
        viewModelScope.launch {
            settingsRepository.save(setup)
            val id = gameRepository.createGame(setup.settings, setup.activeNames)
            _isStarting.value = false
            onCreated(id)
        }
    }

    private companion object {
        const val MAX_NAME = 20
        const val MARRIAGE_MIN = 0
        const val MARRIAGE_MAX = 500
    }
}
