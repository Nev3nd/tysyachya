package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/** Номинал карты и его стоимость. Всего в колоде по [COPIES] карты каждого номинала. */
@Serializable
enum class Rank(val label: String, val points: Int) {
    NINE("9", 0),
    TEN("10", 10),
    JACK("J", 2),
    QUEEN("Q", 3),
    KING("K", 4),
    ACE("A", 11);

    companion object {
        const val COPIES = 4
    }
}
