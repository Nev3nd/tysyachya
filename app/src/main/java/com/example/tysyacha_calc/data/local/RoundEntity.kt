package com.example.tysyacha_calc.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "rounds",
    primaryKeys = ["gameId", "roundNumber"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("gameId")],
)
data class RoundEntity(
    val gameId: Long,
    val roundNumber: Int,
    /** [com.example.tysyacha_calc.data.model.GameRound] в JSON. */
    val payloadJson: String,
)
