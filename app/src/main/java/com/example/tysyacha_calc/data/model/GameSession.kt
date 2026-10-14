package com.example.tysyacha_calc.data.model

/**
 * Сохраняемая партия: настройки, игроки и список конов.
 * Всё остальное (счёт, болты, бочка, дилер, использованные росписи) выводится из [rounds].
 */
data class GameSession(
    val settings: GameSettings,
    val players: List<Player>,
    val rounds: List<GameRound> = emptyList(),
) {
    init {
        require(players.size == settings.playerCount) {
            "Игроков ${players.size}, а в настройках ${settings.playerCount}"
        }
        require(players.map { it.id }.toSet().size == players.size) { "id игроков должны быть уникальны" }
    }
}
