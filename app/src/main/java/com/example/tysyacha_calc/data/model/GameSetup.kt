package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/** Заготовка новой партии: настройки и имена игроков. Сохраняется между запусками. */
@Serializable
data class GameSetup(
    val settings: GameSettings = GameSettings(),
    /** Всегда 4 имени — при игре втроём берутся первые три. */
    val playerNames: List<String> = List(MAX_PLAYERS) { "" },
) {
    val activeNames: List<String> get() = playerNames.take(settings.playerCount)

    companion object {
        const val MAX_PLAYERS = 4
    }
}
