package com.example.tysyacha_calc.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object NewGameRoute

/** Имя поля gameId совпадает с [com.example.tysyacha_calc.viewmodel.GAME_ID_ARG]. */
@Serializable
data class CalculatorRoute(val gameId: Long)

@Serializable
data class HistoryRoute(val gameId: Long)
