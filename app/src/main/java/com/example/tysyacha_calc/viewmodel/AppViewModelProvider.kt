package com.example.tysyacha_calc.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tysyacha_calc.TysyachaApp

/** Фабрика всех ViewModel приложения: берёт зависимости из [com.example.tysyacha_calc.AppContainer]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { HomeViewModel(app().container.gameRepository) }
        initializer {
            SettingsViewModel(app().container.settingsRepository, app().container.gameRepository)
        }
        initializer { CalculatorViewModel(createSavedStateHandle(), app().container.gameRepository) }
        initializer { HistoryViewModel(createSavedStateHandle(), app().container.gameRepository) }
    }

    private fun CreationExtras.app(): TysyachaApp = this[APPLICATION_KEY] as TysyachaApp
}
