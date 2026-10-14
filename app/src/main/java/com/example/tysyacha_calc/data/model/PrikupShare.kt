package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/** При 4 игроках раздающий не играет и записывает себе очки карт прикупа. */
@Serializable
data class PrikupShare(
    val playerId: Int,
    /** Очки карт прикупа без округления. */
    val cardPoints: Int,
    val cards: Map<Rank, Int> = emptyMap(),
)
