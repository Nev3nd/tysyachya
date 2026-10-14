package com.example.tysyacha_calc.data.model

import com.example.tysyacha_calc.domain.CutRitualEngine
import kotlinx.serialization.Serializable

@Serializable
data class CutRitualResult(
    val ninesCount: Int = 0,
    val jacksCount: Int = 0,
) {
    /** Штраф тому, кто сдвигал (игрок справа от дилера). */
    val cutterPenalty: Int get() = CutRitualEngine.penaltyFor(ninesCount)

    val dealerPenalty: Int get() = CutRitualEngine.penaltyFor(jacksCount)

    val isEmpty: Boolean get() = ninesCount == 0 && jacksCount == 0
}
