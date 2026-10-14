package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/** Игрок. Порядок в списке игроков = порядок мест за столом (по ходу игры). */
@Serializable
data class Player(
    val id: Int,
    val name: String,
)
