package com.example.tysyacha_calc.repository

import com.example.tysyacha_calc.data.local.AppJson
import com.example.tysyacha_calc.data.local.GameDao
import com.example.tysyacha_calc.data.local.GameEntity
import com.example.tysyacha_calc.data.local.RoundEntity
import com.example.tysyacha_calc.data.model.GameRound
import com.example.tysyacha_calc.data.model.GameSession
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.GameSummary
import com.example.tysyacha_calc.data.model.GameTimeline
import com.example.tysyacha_calc.data.model.Player
import com.example.tysyacha_calc.domain.GameEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/**
 * Партии и их коны (Room). Храним только исходные данные конов и позицию в истории;
 * счёт, бочка, болты и т. д. каждый раз пересчитываются [GameEngine.replay].
 */
class GameRepository(
    private val dao: GameDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    fun observeGames(): Flow<List<GameSummary>> = dao.observeGames()
        .map { list -> list.map { it.toSummary() } }
        .flowOn(Dispatchers.Default)

    /** Партия с историей; null — если удалена или не читается. */
    fun observeGame(id: Long): Flow<GameTimeline?> =
        combine(dao.observeGame(id), dao.observeRounds(id)) { game, rounds ->
            game?.let { runCatching { it.toTimeline(rounds) }.getOrNull() }
        }.flowOn(Dispatchers.Default)

    suspend fun createGame(settings: GameSettings, names: List<String>): Long {
        val state = GameEngine.newGame(settings, names)
        val time = now()
        return dao.insertGame(
            GameEntity(
                createdAt = time,
                updatedAt = time,
                settingsJson = AppJson.encodeToString(GameSettings.serializer(), state.settings),
                playersJson = AppJson.encodeToString(playersSerializer, state.players),
                scoresJson = encodeScores(state),
            )
        )
    }

    /**
     * Записать последний кон из [newState] (состояние уже после кона).
     * Если до этого вернулись назад — все сохранённые коны после позиции заменяются новым.
     */
    suspend fun saveRound(gameId: Long, newState: GameState) {
        val game = dao.getGame(gameId) ?: return
        val round = newState.rounds.last()
        dao.replaceRoundsFrom(
            game.withState(newState, position = null),
            RoundEntity(gameId, round.roundNumber, AppJson.encodeToString(GameRound.serializer(), round)),
        )
    }

    /** Перейти к позиции в истории (0 — до первого кона). Коны не удаляются. */
    suspend fun moveTo(gameId: Long, position: Int) {
        val game = dao.getGame(gameId) ?: return
        val rounds = dao.getRounds(gameId)
        val target = position.coerceIn(0, rounds.size)
        val state = GameEngine.replay(game.toSession(rounds.take(target)))
        dao.updateGame(game.withState(state, position = target.takeIf { it < rounds.size }))
    }

    suspend fun deleteGame(gameId: Long) = dao.deleteGame(gameId)

    private fun GameEntity.toTimeline(rounds: List<RoundEntity>): GameTimeline {
        val session = toSession(rounds)
        val full = GameEngine.replay(session)
        val pos = position?.coerceIn(0, rounds.size) ?: rounds.size
        val state = if (pos == rounds.size) full else GameEngine.replay(session.copy(rounds = session.rounds.take(pos)))
        return GameTimeline(state = state, full = full, position = pos)
    }

    private fun GameEntity.withState(state: GameState, position: Int?) = copy(
        updatedAt = now(),
        scoresJson = encodeScores(state),
        roundsCount = state.rounds.size,
        winnerId = state.winnerId,
        position = position,
    )

    private fun GameEntity.toSession(rounds: List<RoundEntity>) = GameSession(
        settings = AppJson.decodeFromString(GameSettings.serializer(), settingsJson),
        players = AppJson.decodeFromString(playersSerializer, playersJson),
        rounds = rounds.map(::decodeRound),
    )

    private fun GameEntity.toSummary() = GameSummary(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        players = AppJson.decodeFromString(playersSerializer, playersJson),
        scores = AppJson.decodeFromString(scoresSerializer, scoresJson),
        roundsCount = roundsCount,
        winnerId = winnerId,
    )

    private fun decodeRound(entity: RoundEntity): GameRound =
        AppJson.decodeFromString(GameRound.serializer(), entity.payloadJson)

    private fun encodeScores(state: GameState): String =
        AppJson.encodeToString(scoresSerializer, state.playerStates.map { it.score })

    private companion object {
        val playersSerializer = ListSerializer(Player.serializer())
        val scoresSerializer = ListSerializer(Int.serializer())
    }
}
