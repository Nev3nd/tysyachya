package com.example.tysyacha_calc.data.model

import kotlinx.serialization.Serializable

/** Итог торгов: кто забрал прикуп и какая ставка. */
@Serializable
data class Bid(
    val takerId: Int,
    val amount: Int,
)
