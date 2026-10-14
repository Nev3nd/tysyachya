package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.GameState
import com.example.tysyacha_calc.data.model.RoundResult

sealed interface RoundOutcome {
    data class Success(val state: GameState, val result: RoundResult) : RoundOutcome
    data class Invalid(val errors: List<RoundError>) : RoundOutcome
}
