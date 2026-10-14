package com.example.tysyacha_calc.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tysyacha_calc.data.local.AppJson
import com.example.tysyacha_calc.data.model.GameSetup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Настройки новой партии и имена игроков (DataStore). */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val setup: Flow<GameSetup> = dataStore.data
        .map { prefs -> prefs[SETUP_KEY]?.let(::decode) ?: GameSetup() }
        .distinctUntilChanged()

    suspend fun save(setup: GameSetup) {
        dataStore.edit { it[SETUP_KEY] = AppJson.encodeToString(GameSetup.serializer(), setup) }
    }

    private fun decode(raw: String): GameSetup? =
        runCatching { AppJson.decodeFromString(GameSetup.serializer(), raw) }.getOrNull()
            ?.let { s -> s.copy(playerNames = (s.playerNames + List(GameSetup.MAX_PLAYERS) { "" }).take(GameSetup.MAX_PLAYERS)) }

    private companion object {
        val SETUP_KEY = stringPreferencesKey("game_setup")
    }
}
