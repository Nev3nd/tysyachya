package com.example.tysyacha_calc

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.tysyacha_calc.data.local.AppDatabase
import com.example.tysyacha_calc.repository.GameRepository
import com.example.tysyacha_calc.repository.SettingsRepository

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Ручное внедрение зависимостей: один экземпляр на приложение. */
class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)

    val gameRepository = GameRepository(database.gameDao())
    val settingsRepository = SettingsRepository(context.settingsDataStore)
}
