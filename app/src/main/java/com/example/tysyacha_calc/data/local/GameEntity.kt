package com.example.tysyacha_calc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val updatedAt: Long,
    /** Снимок [com.example.tysyacha_calc.data.model.GameSettings] на момент начала партии. */
    val settingsJson: String,
    val playersJson: String,
    /** Кэш для списка партий, чтобы не пересчитывать все партии ради главного экрана. */
    val scoresJson: String,
    /** Сколько конов применено на текущей позиции (для списка партий). */
    val roundsCount: Int = 0,
    val winnerId: Int? = null,
    /**
     * Позиция в истории: сколько конов из сохранённых сейчас применено.
     * null — все. Меньше числа конов — пользователь вернулся назад; «будущие» коны
     * не удаляются, пока он не запишет новый кон с этой позиции.
     */
    val position: Int? = null,
)
