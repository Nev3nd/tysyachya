package com.example.tysyacha_calc.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM games ORDER BY updatedAt DESC")
    fun observeGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun observeGame(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber")
    fun observeRounds(gameId: Long): Flow<List<RoundEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGame(id: Long): GameEntity?

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber")
    suspend fun getRounds(gameId: Long): List<RoundEntity>

    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Insert
    suspend fun insertRound(round: RoundEntity)

    @Query("DELETE FROM rounds WHERE gameId = :gameId AND roundNumber >= :fromRound")
    suspend fun deleteRoundsFrom(gameId: Long, fromRound: Int)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGame(id: Long)

    /** Записать кон; все сохранённые коны с этим номером и дальше заменяются. */
    @Transaction
    suspend fun replaceRoundsFrom(game: GameEntity, round: RoundEntity) {
        deleteRoundsFrom(game.id, round.roundNumber)
        insertRound(round)
        updateGame(game)
    }
}
